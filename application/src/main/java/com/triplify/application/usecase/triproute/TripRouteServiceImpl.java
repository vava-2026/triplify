package com.triplify.application.usecase.triproute;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.GetRouteByIdRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.triproute.dto.AddTripRouteRequest;
import com.triplify.application.usecase.triproute.dto.DeleteTripRouteRequest;
import com.triplify.application.usecase.triproute.dto.GetTripRouteByIdRequest;
import com.triplify.application.usecase.triproute.dto.GetTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.RearrangeTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.ReplaceTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.application.usecase.triproute.dto.UpdateTripRouteRequest;
import com.triplify.application.usecase.triproute.dto.UpdateTripRouteStatusRequest;
import com.triplify.domain.error.RouteError;
import com.triplify.domain.error.TripError;
import com.triplify.domain.error.TripRouteError;
import com.triplify.domain.model.RoutePlace;
import com.triplify.domain.model.Trip;
import com.triplify.domain.model.TripPlace;
import com.triplify.domain.model.TripRoute;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.RoutePlaceRepository;
import com.triplify.domain.repository.RouteRepository;
import com.triplify.domain.repository.TripPlaceRepository;
import com.triplify.domain.repository.TripRepository;
import com.triplify.domain.repository.TripRouteRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Authenticated
public class TripRouteServiceImpl implements TripRouteService {

    private static final Logger log = LoggerFactory.getLogger(TripRouteServiceImpl.class);

    private final TripRouteRepository tripRouteRepository;
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final RoutePlaceRepository routePlaceRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final RouteService routeService;
    private final UserSessionContext userSessionContext;

    @Inject
    TripRouteServiceImpl(
            TripRouteRepository tripRouteRepository,
            TripRepository tripRepository,
            RouteRepository routeRepository,
            RoutePlaceRepository routePlaceRepository,
            TripPlaceRepository tripPlaceRepository,
            RouteService routeService,
            UserSessionContext userSessionContext
    ) {
        this.tripRouteRepository = tripRouteRepository;
        this.tripRepository = tripRepository;
        this.routeRepository = routeRepository;
        this.routePlaceRepository = routePlaceRepository;
        this.tripPlaceRepository = tripPlaceRepository;
        this.routeService = routeService;
        this.userSessionContext = userSessionContext;
    }

    @Override
    public Result<TripRouteResponse> addTripRoute(AddTripRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        requireOwnedTrip(request.tripId(), user.userId()).orThrow();

        if (routeRepository.findById(request.routeId()).isEmpty()) {
            return Result.fail(new RouteError.NotFound(request.routeId().toString()));
        }

        var existing = tripRouteRepository.findByTripIdAndRouteId(request.tripId(), request.routeId());
        if (existing.isPresent()) {
            return getTripRouteById(new GetTripRouteByIdRequest(existing.get().getId()));
        }

        TripRoute tripRoute = new TripRoute(
                request.tripId(),
                request.routeId(),
                request.order()
        );
        tripRouteRepository.create(tripRoute);
        syncRouteDerivedPlaces(tripRoute.getTripId());
        return getTripRouteById(new GetTripRouteByIdRequest(tripRoute.getId()));
    }

    @Override
    public Result<TripRouteResponse> updateTripRoute(UpdateTripRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        TripRoute existing = requireTripRoute(request.id()).orThrow();
        requireOwnedTrip(existing.getTripId(), user.userId()).orThrow();

        TripRoute updated = new TripRoute(
                existing.getId(),
                existing.getTripId(),
                existing.getRouteId(),
                existing.getRoute(),
                request.order(),
                existing.getStatus(),
                existing.getStartedAt(),
                existing.getEndedAt(),
                existing.getCreatedAt(),
                Instant.now()
        );
        tripRouteRepository.update(updated);
        return getTripRouteById(new GetTripRouteByIdRequest(updated.getId()));
    }

    @Override
    public Result<Void> deleteTripRoute(DeleteTripRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        TripRoute existing = requireTripRoute(request.id()).orThrow();
        requireOwnedTrip(existing.getTripId(), user.userId()).orThrow();

        UUID tripId = existing.getTripId();
        tripRouteRepository.delete(existing);
        resequenceTripRoutes(tripId);
        syncRouteDerivedPlaces(tripId);
        return Result.ok();
    }

    @Override
    public Result<TripRouteResponse> rearrangeTripRoutes(RearrangeTripRoutesRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        requireOwnedTrip(request.id(), user.userId()).orThrow();
        if (request.routesIdsInOrder() == null) {
            return Result.fail(new ApplicationError.Unexpected("Trip route order is required"));
        }

        Page<TripRoute> currentPage = tripRouteRepository.findList(new PageRequest(0, 512), request.id(), null);
        List<TripRoute> currentRoutes = currentPage.items();
        List<UUID> requestedRouteIds = new ArrayList<>(request.routesIdsInOrder());

        if (requestedRouteIds.size() != currentRoutes.size()) {
            return Result.fail(new ApplicationError.Unexpected("Trip route order does not match current routes"));
        }

        Set<UUID> currentRouteIds = currentRoutes.stream()
                .map(TripRoute::getRouteId)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        if (!currentRouteIds.equals(new HashSet<>(requestedRouteIds))) {
            return Result.fail(new ApplicationError.Unexpected("Trip route order does not match current routes"));
        }

        for (int i = 0; i < requestedRouteIds.size(); i++) {
            UUID routeId = requestedRouteIds.get(i);
            TripRoute tripRoute = currentRoutes.stream()
                    .filter(item -> routeId.equals(item.getRouteId()))
                    .findFirst()
                    .orElse(null);
            if (tripRoute == null || tripRoute.getOrder() == i) {
                continue;
            }

            TripRoute updated = new TripRoute(
                    tripRoute.getId(),
                    tripRoute.getTripId(),
                    tripRoute.getRouteId(),
                    tripRoute.getRoute(),
                    i,
                    tripRoute.getStatus(),
                    tripRoute.getStartedAt(),
                    tripRoute.getEndedAt(),
                    tripRoute.getCreatedAt(),
                    Instant.now()
            );
            tripRouteRepository.update(updated);
        }

        if (requestedRouteIds.isEmpty()) {
            return Result.fail(new ApplicationError.Unexpected("Trip route order is empty"));
        }

        var reordered = tripRouteRepository.findByTripIdAndRouteId(request.id(), requestedRouteIds.get(0));
        if (reordered.isEmpty()) {
            return Result.fail(new TripRouteError.NotFound(request.id().toString()));
        }
        return getTripRouteById(new GetTripRouteByIdRequest(reordered.get().getId()));
    }

    @Override
    public Result<TripRouteResponse> updateStatus(UpdateTripRouteStatusRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        TripRoute existing = requireTripRoute(request.id()).orThrow();
        requireOwnedTrip(existing.getTripId(), user.userId()).orThrow();

        validateStatusTransition(existing.getStatus(), request.status()).orThrow();

        Instant startedAt = request.startedAt() != null ? request.startedAt() : existing.getStartedAt();
        Instant endedAt = request.endedAt() != null ? request.endedAt() : existing.getEndedAt();
        validateDates(startedAt, endedAt).orThrow();

        TripRoute updated = new TripRoute(
                existing.getId(),
                existing.getTripId(),
                existing.getRouteId(),
                existing.getRoute(),
                existing.getOrder(),
                request.status(),
                startedAt,
                endedAt,
                existing.getCreatedAt(),
                Instant.now()
        );
        tripRouteRepository.update(updated);
        return getTripRouteById(new GetTripRouteByIdRequest(updated.getId()));
    }

    @Override
    public Result<TripRouteResponse> getTripRouteById(GetTripRouteByIdRequest request) {
        return toResponse(requireTripRoute(request.id()).orThrow());
    }

    @Override
    public Result<Page<TripRouteResponse>> getTripRoutes(GetTripRoutesRequest request) {
        GetTripRoutesRequest.Filter filter = request.filter();
        Page<TripRoute> page = tripRouteRepository.findList(
                request.pageRequest(),
                filter == null ? null : filter.tripId(),
                filter == null ? null : filter.status()
        );

        List<TripRouteResponse> responses = new ArrayList<>(page.items().size());
        for (TripRoute tripRoute : page.items()) {
            responses.add(toResponse(tripRoute).orThrow());
        }

        return Result.ok(new Page<>(responses, page.page(), page.size(), page.hasNext()));
    }

    @Override
    public Result<Void> replaceTripRoutes(ReplaceTripRoutesRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        requireOwnedTrip(request.tripId(), user.userId()).orThrow();

        List<TripRoute> existing = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, 512);
        while (true) {
            Page<TripRoute> page = tripRouteRepository.findList(pageRequest, request.tripId(), null);
            existing.addAll(page.items());
            if (!page.hasNext()) break;
            pageRequest = pageRequest.next();
        }
        for (TripRoute tripRoute : existing) {
            tripRouteRepository.delete(tripRoute);
        }

        List<UUID> routeIds = request.routeIdsInOrder();
        for (int i = 0; i < routeIds.size(); i++) {
            tripRouteRepository.create(new TripRoute(request.tripId(), routeIds.get(i), i));
        }

        syncRouteDerivedPlaces(request.tripId());
        log.info("Replaced trip routes for tripId='{}' with {} routes by userId='{}'", request.tripId(), routeIds.size(), user.userId());
        return Result.ok();
    }

    @Override
    public Result<List<TripRouteResponse>> getAllTripRoutes(UUID tripId) {
        List<TripRouteResponse> items = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, 100);
        while (true) {
            var result = getTripRoutes(new GetTripRoutesRequest(pageRequest, new GetTripRoutesRequest.Filter(tripId, null)));
            if (result.isFailure()) return Result.fail(result.getError());
            Page<TripRouteResponse> page = result.getValue();
            items.addAll(page.items());
            if (!page.hasNext()) return Result.ok(items);
            pageRequest = pageRequest.next();
        }
    }

    private Result<Trip> requireOwnedTrip(UUID tripId, UUID userId) {
        var tripRes = tripRepository.findById(tripId);
        if (tripRes.isEmpty()) {
            return Result.fail(new TripError.NotFound(tripId.toString()));
        }

        Trip trip = tripRes.get();
        if (!Objects.equals(trip.getUserId(), userId)) {
            return Result.fail(new TripError.NotOwner(tripId.toString()));
        }

        return Result.ok(trip);
    }

    private Result<TripRoute> requireTripRoute(UUID tripRouteId) {
        return tripRouteRepository.findById(tripRouteId)
                .<Result<TripRoute>>map(Result::ok)
                .orElseGet(() -> Result.fail(new TripRouteError.NotFound(tripRouteId.toString())));
    }

    private Result<Void> validateStatusTransition(StatusEnum from, StatusEnum to) {
        if (from == to) {
            return Result.ok();
        }

        boolean valid = switch (from) {
            case PLANNED -> to == StatusEnum.ONGOING || to == StatusEnum.VISITED || to == StatusEnum.CANCELED;
            case ONGOING -> to == StatusEnum.VISITED || to == StatusEnum.CANCELED;
            case VISITED, CANCELED -> false;
        };

        if (!valid) {
            return Result.fail(new TripRouteError.InvalidStatusTransition(from.getValue(), to.getValue()));
        }
        return Result.ok();
    }

    private Result<Void> validateDates(Instant startedAt, Instant endedAt) {
        if (startedAt != null && endedAt != null && endedAt.isBefore(startedAt)) {
            return Result.fail(new ApplicationError.Unexpected(
                    "Trip route start date %s must be before end date %s".formatted(
                            startedAt.atZone(ZoneOffset.UTC).toLocalDate(),
                            endedAt.atZone(ZoneOffset.UTC).toLocalDate()
                    )
            ));
        }
        return Result.ok();
    }

    private Result<TripRouteResponse> toResponse(TripRoute tripRoute) {
        RouteResponse route = routeService.getRouteById(
                new GetRouteByIdRequest(tripRoute.getRouteId())
        ).orThrow();

        return Result.ok(new TripRouteResponse(
                tripRoute.getId(),
                tripRoute.getTripId(),
                route,
                tripRoute.getOrder(),
                tripRoute.getStatus(),
                tripRoute.getStartedAt(),
                tripRoute.getEndedAt(),
                tripRoute.getCreatedAt(),
                tripRoute.getUpdatedAt(),
                Set.<ImageResponse>of()
        ));
    }

    private void resequenceTripRoutes(UUID tripId) {
        List<TripRoute> remaining = tripRouteRepository.findList(new PageRequest(0, 512), tripId, null).items();
        for (int i = 0; i < remaining.size(); i++) {
            TripRoute item = remaining.get(i);
            if (item.getOrder() == i) {
                continue;
            }

            TripRoute updated = new TripRoute(
                    item.getId(),
                    item.getTripId(),
                    item.getRouteId(),
                    item.getRoute(),
                    i,
                    item.getStatus(),
                    item.getStartedAt(),
                    item.getEndedAt(),
                    item.getCreatedAt(),
                    Instant.now()
            );
            tripRouteRepository.update(updated);
        }
    }

    private void syncRouteDerivedPlaces(UUID tripId) {
        removeExistingRouteDerivedPlaces(tripId);

        List<TripRoute> tripRoutes = tripRouteRepository.findList(new PageRequest(0, 512), tripId, null).items();
        for (TripRoute tripRoute : tripRoutes) {
            List<RoutePlace> routePlaces = routePlaceRepository.findByRouteId(tripRoute.getRouteId());
            for (RoutePlace routePlace : routePlaces) {
                if (tripPlaceRepository.findByTripIdAndPlaceId(tripId, routePlace.getPlaceId()).isPresent()) {
                    continue;
                }
                tripPlaceRepository.create(new TripPlace(
                        tripRoute.getTripId(),
                        routePlace.getPlaceId(),
                        tripRoute.getId(),
                        routePlace.getId()
                ));
            }
        }
    }

    private void removeExistingRouteDerivedPlaces(UUID tripId) {
        PageRequest pageRequest = new PageRequest(0, 512);
        while (true) {
            Page<TripPlace> page = tripPlaceRepository.findList(
                    pageRequest,
                    tripId,
                    TripPlaceSourceType.ROUTE,
                    null,
                    null,
                    null,
                    null,
                    true
            );
            for (TripPlace tripPlace : page.items()) {
                tripPlaceRepository.delete(tripPlace);
            }
            if (!page.hasNext()) {
                return;
            }
            pageRequest = pageRequest.next();
        }
    }
}
