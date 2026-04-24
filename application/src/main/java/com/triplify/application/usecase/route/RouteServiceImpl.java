package com.triplify.application.usecase.route;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.geo.GeoCalculator;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.dto.AddPlaceToRouteRequest;
import com.triplify.application.usecase.route.dto.AddRouteRequest;
import com.triplify.application.usecase.route.dto.DeletePlaceFromRouteRequest;
import com.triplify.application.usecase.route.dto.DeleteRouteRequest;
import com.triplify.application.usecase.route.dto.GetRouteByIdRequest;
import com.triplify.application.usecase.route.dto.GetRoutesRequest;
import com.triplify.application.usecase.route.dto.RearrangePlacesInRouteRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.route.dto.UpdateRouteRequest;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.error.PlaceError;
import com.triplify.domain.model.Place;
import com.triplify.domain.error.RouteError;
import com.triplify.domain.filter.RouteFilter;
import com.triplify.domain.model.Route;
import com.triplify.domain.model.RoutePlace;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.PlaceRepository;
import com.triplify.domain.repository.RoutePlaceRepository;
import com.triplify.domain.repository.RouteRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Authenticated
public class RouteServiceImpl implements RouteService {
    private static final Logger log = LoggerFactory.getLogger(RouteServiceImpl.class);
    private static final String DEFAULT_IMAGE_DESCRIPTION = "Cover image for route ";

    private final RouteRepository routeRepository;
    private final RoutePlaceRepository routePlaceRepository;
    private final PlaceRepository placeRepository;
    private final UserSessionContext userSessionContext;
    private final ImageService imageService;

    @Inject
    RouteServiceImpl(
            RouteRepository routeRepository,
            RoutePlaceRepository routePlaceRepository,
            PlaceRepository placeRepository,
            UserSessionContext userSessionContext,
            ImageService imageService
    ) {
        this.routeRepository = routeRepository;
        this.routePlaceRepository = routePlaceRepository;
        this.placeRepository = placeRepository;
        this.userSessionContext = userSessionContext;
        this.imageService = imageService;
    }

    @Override
    public Result<RouteResponse> addRoute(AddRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();

        Route route = new Route(
                user.userId(),
                request.title(),
                normalizeDescription(request.description()),
                calculateLength(List.of())
        );

        replaceCoverImage(route, request.coverImage(), request.title()).orThrow();

        routeRepository.create(route);
        log.info("Added new route with id='{}', title='{}' by userId='{}'", route.getId(), route.getTitle(), user.userId());
        return Result.ok(RouteResponse.from(route, List.of()));
    }

    @Override
    public Result<RouteResponse> updateRoute(UpdateRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Route route = requireOwnedRoute(request.id(), user.userId()).orThrow();
        List<RoutePlace> routePlaces = routePlaceRepository.findByRouteId(request.id());
        route.updateTitle(request.title());
        route.updateDescription(normalizeDescription(request.description()));
        route.updateLength(calculateLength(routePlaces));

        replaceCoverImage(route, request.coverImage(), request.title()).orThrow();

        routeRepository.update(route);
        log.info("Updated route with id='{}', title='{}' by userId='{}'", route.getId(), route.getTitle(), user.userId());
        return getRouteById(new GetRouteByIdRequest(request.id()));
    }

    @Override
    public Result<Void> deleteRoute(DeleteRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Route route = requireOwnedRoute(request.id(), user.userId()).orThrow();
        if (route.getCoverImage() != null) {
            imageService.deleteImage(new DeleteImageRequest(route.getCoverImage().getId())).orThrow();
            route.removeCoverImage();
        }
        routeRepository.delete(route);
        log.info("Deleted route with id='{}' by userId='{}'", request.id(), user.userId());
        return Result.ok();
    }

    @Override
    public Result<RouteResponse> addPlaceToRoute(AddPlaceToRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Route route = requireOwnedRoute(request.routeId(), user.userId()).orThrow();

        if (placeRepository.findById(request.placeId()).isEmpty()) {
            return Result.fail(new PlaceError.NotFound(request.placeId().toString()));
        }

        if (routePlaceRepository.findByRouteIdAndPlaceId(request.routeId(), request.placeId()).isPresent()) {
            return Result.ok(RouteResponse.from(route, routePlaceRepository.findByRouteId(request.routeId())));
        }

        List<RoutePlace> currentPlaces = routePlaceRepository.findByRouteId(request.routeId());
        int nextOrder = currentPlaces.stream()
                .mapToInt(RoutePlace::getOrder)
                .max()
                .orElse(-1) + 1;

        RoutePlace routePlace = new RoutePlace(route.getId(), request.placeId(), nextOrder);
        routePlaceRepository.create(routePlace);
        List<RoutePlace> updatedRoutePlaces = routePlaceRepository.findByRouteId(request.routeId());
        route.updateLength(calculateLength(updatedRoutePlaces));
        routeRepository.update(route);

        log.info("Added placeId='{}' to routeId='{}' by userId='{}'", request.placeId(), request.routeId(), user.userId());
        return getRouteById(new GetRouteByIdRequest(request.routeId()));
    }

    @Override
    public Result<RouteResponse> deletePlaceFromRoute(DeletePlaceFromRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Route route = requireOwnedRoute(request.routeId(), user.userId()).orThrow();

        var routePlaceRes = routePlaceRepository.findByRouteIdAndPlaceId(request.routeId(), request.placeId());
        if (routePlaceRes.isEmpty()) {
            return Result.fail(new RouteError.PlaceNotInRoute(request.placeId().toString(), request.routeId().toString()));
        }

        routePlaceRepository.delete(routePlaceRes.get());
        resequenceRoutePlaces(request.routeId());
        List<RoutePlace> updatedRoutePlaces = routePlaceRepository.findByRouteId(request.routeId());
        route.updateLength(calculateLength(updatedRoutePlaces));
        routeRepository.update(route);

        log.info("Deleted placeId='{}' from routeId='{}' by userId='{}'", request.placeId(), request.routeId(), user.userId());
        return Result.ok(RouteResponse.from(route, updatedRoutePlaces));
    }

    @Override
    public Result<RouteResponse> rearrangePlacesInRoute(RearrangePlacesInRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Route route = requireOwnedRoute(request.id(), user.userId()).orThrow();

        List<UUID> placeIdsInOrder = request.placeIdsInOrder();
        if (placeIdsInOrder == null) {
            return Result.fail(new ApplicationError.Unexpected("Route place order is required"));
        }

        List<RoutePlace> routePlaces = routePlaceRepository.findByRouteId(request.id());
        if (placeIdsInOrder.size() != routePlaces.size()) {
            return Result.fail(new ApplicationError.Unexpected("Route place order does not match current route places"));
        }

        Set<UUID> requestedIds = new HashSet<>(placeIdsInOrder);
        Set<UUID> currentIds = routePlaces.stream()
                .map(RoutePlace::getPlaceId)
                .collect(Collectors.toSet());
        if (requestedIds.size() != placeIdsInOrder.size() || !requestedIds.equals(currentIds)) {
            return Result.fail(new ApplicationError.Unexpected("Route place order does not match current route places"));
        }

        Map<UUID, RoutePlace> routePlaceById = new HashMap<>();
        for (RoutePlace routePlace : routePlaces) {
            routePlaceById.put(routePlace.getPlaceId(), routePlace);
        }

        for (int i = 0; i < placeIdsInOrder.size(); i++) {
            RoutePlace routePlace = routePlaceById.get(placeIdsInOrder.get(i));
            if (routePlace.getOrder() != i) {
                routePlace.updateOrder(i);
                routePlaceRepository.update(routePlace);
            }
        }
        List<RoutePlace> updatedRoutePlaces = routePlaceRepository.findByRouteId(request.id());
        route.updateLength(calculateLength(updatedRoutePlaces));
        routeRepository.update(route);

        log.info("Rearranged places in routeId='{}' by userId='{}'", request.id(), user.userId());
        return Result.ok(RouteResponse.from(route, updatedRoutePlaces));
    }

    @Override
    public Result<RouteResponse> getRouteById(GetRouteByIdRequest request) {
        var routeRes = routeRepository.findById(request.id());
        if (routeRes.isEmpty()) {
            log.warn("Attempt to get non-existing route with id='{}'", request.id());
            return Result.fail(new RouteError.NotFound(request.id().toString()));
        }

        return Result.ok(RouteResponse.from(routeRes.get(), routePlaceRepository.findByRouteId(request.id())));
    }

    @Override
    public Result<Page<RouteResponse>> getRoutes(GetRoutesRequest request) {
        RouteFilter filter = new RouteFilter(request.filter() != null ? request.filter().name() : null);
        Page<Route> routesPage = routeRepository.findList(request.pageRequest(), filter);
        Page<RouteResponse> responsePage = routesPage.map(route ->
                RouteResponse.from(route, List.of())
        );
        return Result.ok(responsePage);
    }

    private Result<Route> requireOwnedRoute(UUID routeId, UUID userId) {
        var routeRes = routeRepository.findById(routeId);
        if (routeRes.isEmpty()) {
            log.warn("Attempt to modify non-existing route with id='{}' by userId='{}'", routeId, userId);
            return Result.fail(new RouteError.NotFound(routeId.toString()));
        }

        Route route = routeRes.get();
        if (!route.getUserID().equals(userId)) {
            log.warn("Attempt to modify route with id='{}' owned by userId='{}' from userId='{}'", routeId, route.getUserID(), userId);
            return Result.fail(new RouteError.NotOwner(routeId.toString()));
        }

        return Result.ok(route);
    }

    private Result<Void> replaceCoverImage(Route route, Path newCoverImage, String title) {
        if (newCoverImage == null) {
            if (route.getCoverImage() != null) {
                imageService.deleteImage(new DeleteImageRequest(route.getCoverImage().getId())).orThrow();
                route.removeCoverImage();
            }
            return Result.ok();
        }

        if (route.getCoverImage() != null && route.getCoverImage().getUrl().equals(newCoverImage)) {
            return Result.ok();
        }

        if (route.getCoverImage() != null) {
            imageService.deleteImage(new DeleteImageRequest(route.getCoverImage().getId())).orThrow();
            route.removeCoverImage();
        }

        ImageResponse image = imageService.addImage(new AddImageRequest(newCoverImage, DEFAULT_IMAGE_DESCRIPTION + title)).orThrow();
        route.updateCoverImage(image.id());
        return Result.ok();
    }

    private void resequenceRoutePlaces(UUID routeId) {
        List<RoutePlace> remainingPlaces = routePlaceRepository.findByRouteId(routeId);
        for (int i = 0; i < remainingPlaces.size(); i++) {
            RoutePlace routePlace = remainingPlaces.get(i);
            if (routePlace.getOrder() != i) {
                routePlace.updateOrder(i);
                routePlaceRepository.update(routePlace);
            }
        }
    }

    private double calculateLength(List<RoutePlace> routePlaces) {
        if (routePlaces == null || routePlaces.size() < 2) {
            return 0D;
        }

        double totalLength = 0D;
        for (int i = 1; i < routePlaces.size(); i++) {
            Place from = routePlaces.get(i - 1).getPlace();
            Place to = routePlaces.get(i).getPlace();
            if (from == null || to == null) {
                continue;
            }
            totalLength += GeoCalculator.distanceKm(
                    from.getLatitude(),
                    from.getLongitude(),
                    to.getLatitude(),
                    to.getLongitude()
            );
        }

        return totalLength;
    }


    private String normalizeDescription(String description) {
        return Objects.requireNonNullElse(description, "");
    }
}
