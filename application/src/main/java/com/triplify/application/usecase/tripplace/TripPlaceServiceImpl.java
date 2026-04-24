package com.triplify.application.usecase.tripplace;

import com.google.inject.Inject;
import com.triplify.application.shared.error.ApplicationError;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.tripplace.dto.AddTripPlaceRequest;
import com.triplify.application.usecase.tripplace.dto.DeleteTripPlaceRequest;
import com.triplify.application.usecase.tripplace.dto.GetTripPlaceByIdRequest;
import com.triplify.application.usecase.tripplace.dto.GetTripPlacesRequest;
import com.triplify.application.usecase.tripplace.dto.ReplaceManualTripPlacesRequest;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.tripplace.dto.UpdateTripPlaceRequest;
import com.triplify.domain.error.PlaceError;
import com.triplify.domain.error.TripError;
import com.triplify.domain.error.TripPlaceError;
import com.triplify.domain.model.RoutePlace;
import com.triplify.domain.model.Trip;
import com.triplify.domain.model.TripPlace;
import com.triplify.domain.model.TripRoute;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.PlaceRepository;
import com.triplify.domain.repository.RoutePlaceRepository;
import com.triplify.domain.repository.TripPlaceRepository;
import com.triplify.domain.repository.TripRepository;
import com.triplify.domain.repository.TripRouteRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Authenticated
public class TripPlaceServiceImpl implements TripPlaceService {

    private static final Logger log = LoggerFactory.getLogger(TripPlaceServiceImpl.class);

    private final TripPlaceRepository tripPlaceRepository;
    private final TripRepository tripRepository;
    private final TripRouteRepository tripRouteRepository;
    private final RoutePlaceRepository routePlaceRepository;
    private final PlaceRepository placeRepository;
    private final PlaceService placeService;
    private final UserSessionContext userSessionContext;

    @Inject
    TripPlaceServiceImpl(
            TripPlaceRepository tripPlaceRepository,
            TripRepository tripRepository,
            TripRouteRepository tripRouteRepository,
            RoutePlaceRepository routePlaceRepository,
            PlaceRepository placeRepository,
            PlaceService placeService,
            UserSessionContext userSessionContext
    ) {
        this.tripPlaceRepository = tripPlaceRepository;
        this.tripRepository = tripRepository;
        this.tripRouteRepository = tripRouteRepository;
        this.routePlaceRepository = routePlaceRepository;
        this.placeRepository = placeRepository;
        this.placeService = placeService;
        this.userSessionContext = userSessionContext;
    }

    @Override
    public Result<TripPlaceResponse> addTripPlace(AddTripPlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        requireOwnedTrip(request.tripId(), user.userId()).orThrow();

        if (placeRepository.findById(request.placeId()).isEmpty()) {
            return Result.fail(new PlaceError.NotFound(request.placeId().toString()));
        }

        var existing = tripPlaceRepository.findByTripIdAndPlaceId(request.tripId(), request.placeId());
        if (existing.isPresent()) {
            return getTripPlaceById(new GetTripPlaceByIdRequest(existing.get().getId()));
        }

        RouteSourceRefs routeSourceRefs = validateRouteSource(request).orThrow();
        TripPlace tripPlace = routeSourceRefs == null
                ? new TripPlace(request.tripId(), request.placeId())
                : new TripPlace(
                        request.tripId(),
                        request.placeId(),
                        routeSourceRefs.tripRouteId(),
                        routeSourceRefs.routePlaceId()
                );

        if (request.visitDate() != null) {
            tripPlace.scheduleVisit(request.visitDate());
        }

        tripPlaceRepository.create(tripPlace);
        return getTripPlaceById(new GetTripPlaceByIdRequest(tripPlace.getId()));
    }

    @Override
    public Result<TripPlaceResponse> updateTripPlace(UpdateTripPlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        TripPlace existing = requireTripPlace(request.id()).orThrow();
        requireOwnedTrip(existing.getTripId(), user.userId()).orThrow();

        TripPlace updated = new TripPlace(
                existing.getId(),
                existing.getTripId(),
                existing.getPlaceId(),
                existing.getPlace(),
                existing.getSourceType(),
                existing.getTripRouteId(),
                existing.getRoutePlaceId(),
                request.visitDate(),
                existing.getCreatedAt(),
                Instant.now()
        );
        tripPlaceRepository.update(updated);
        return getTripPlaceById(new GetTripPlaceByIdRequest(updated.getId()));
    }

    @Override
    public Result<Void> deleteTripPlace(DeleteTripPlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        TripPlace existing = requireTripPlace(request.id()).orThrow();
        requireOwnedTrip(existing.getTripId(), user.userId()).orThrow();

        tripPlaceRepository.delete(existing);
        return Result.ok();
    }

    @Override
    public Result<TripPlaceResponse> getTripPlaceById(GetTripPlaceByIdRequest request) {
        return toResponse(requireTripPlace(request.id()).orThrow());
    }

    @Override
    public Result<Page<TripPlaceResponse>> getTripPlaces(GetTripPlacesRequest request) {
        GetTripPlacesRequest.Filter filter = request.filter();
        GetTripPlacesRequest.OrderBy orderBy = request.orderBy();

        Page<TripPlace> page = tripPlaceRepository.findList(
                request.pageRequest(),
                filter == null ? null : filter.tripId(),
                filter == null ? null : filter.sourceType(),
                filter == null ? null : filter.tripRouteId(),
                filter == null ? null : filter.routePlaceId(),
                filter == null ? null : filter.visitFrom(),
                filter == null ? null : filter.visitTo(),
                orderBy == null || orderBy.visitTimeAsc()
        );

        List<TripPlaceResponse> responses = new ArrayList<>(page.items().size());
        for (TripPlace tripPlace : page.items()) {
            responses.add(toResponse(tripPlace).orThrow());
        }

        return Result.ok(new Page<>(responses, page.page(), page.size(), page.hasNext()));
    }

    @Override
    public Result<Void> replaceManualTripPlaces(ReplaceManualTripPlacesRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        requireOwnedTrip(request.tripId(), user.userId()).orThrow();

        List<TripPlaceResponse> existing = getAllManualTripPlaces(request.tripId()).orThrow();
        for (TripPlaceResponse tripPlace : existing) {
            tripPlaceRepository.findById(tripPlace.id()).ifPresent(tripPlaceRepository::delete);
        }

        for (UUID placeId : request.placeIds()) {
            tripPlaceRepository.create(new TripPlace(request.tripId(), placeId));
        }

        log.info("Replaced manual trip places for tripId='{}' with {} places by userId='{}'", request.tripId(), request.placeIds().size(), user.userId());
        return Result.ok();
    }

    @Override
    public Result<List<TripPlaceResponse>> getAllManualTripPlaces(UUID tripId) {
        List<TripPlaceResponse> items = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, 100);
        while (true) {
            var result = getTripPlaces(new GetTripPlacesRequest(
                    pageRequest,
                    new GetTripPlacesRequest.Filter(tripId, TripPlaceSourceType.MANUAL, null, null, null, null),
                    new GetTripPlacesRequest.OrderBy(true)
            ));
            if (result.isFailure()) return Result.fail(result.getError());
            Page<TripPlaceResponse> page = result.getValue();
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

    private Result<TripPlace> requireTripPlace(UUID tripPlaceId) {
        return tripPlaceRepository.findById(tripPlaceId)
                .<Result<TripPlace>>map(Result::ok)
                .orElseGet(() -> Result.fail(new TripPlaceError.NotFound(tripPlaceId.toString())));
    }

    private Result<RouteSourceRefs> validateRouteSource(AddTripPlaceRequest request) {
        if (request.sourceType() != TripPlaceSourceType.ROUTE) {
            return Result.ok(null);
        }
        if (request.tripRouteId() == null) {
            return Result.fail(new ApplicationError.Unexpected("Trip route id is required for route-derived trip places"));
        }
        if (request.routePlaceId() == null) {
            return Result.fail(new ApplicationError.Unexpected("Route place id is required for route-derived trip places"));
        }

        var tripRouteRes = tripRouteRepository.findById(request.tripRouteId());
        if (tripRouteRes.isEmpty()) {
            return Result.fail(new ApplicationError.Unexpected("Trip route '%s' not found".formatted(request.tripRouteId())));
        }

        TripRoute tripRoute = tripRouteRes.get();
        if (!tripRoute.getTripId().equals(request.tripId())) {
            return Result.fail(new ApplicationError.Unexpected("Trip route does not belong to trip '%s'".formatted(request.tripId())));
        }

        List<RoutePlace> routePlaces = routePlaceRepository.findByRouteId(tripRoute.getRouteId());
        RoutePlace routePlace = routePlaces.stream()
                .filter(item -> item.getId().equals(request.routePlaceId()))
                .findFirst()
                .orElse(null);
        if (routePlace == null) {
            return Result.fail(new ApplicationError.Unexpected("Route place '%s' not found".formatted(request.routePlaceId())));
        }
        if (!routePlace.getPlaceId().equals(request.placeId())) {
            return Result.fail(new ApplicationError.Unexpected("Route place does not match place '%s'".formatted(request.placeId())));
        }

        return Result.ok(new RouteSourceRefs(tripRoute.getId(), routePlace.getId()));
    }

    private Result<TripPlaceResponse> toResponse(TripPlace tripPlace) {
        PlaceResponse place = placeService.getPlaceById(
                new GetPlaceByIdRequest(tripPlace.getPlaceId())
        ).orThrow();

        return Result.ok(new TripPlaceResponse(
                tripPlace.getId(),
                tripPlace.getTripId(),
                place,
                tripPlace.getSourceType(),
                tripPlace.getTripRouteId(),
                tripPlace.getRoutePlaceId(),
                tripPlace.getVisitDate(),
                tripPlace.getCreatedAt(),
                tripPlace.getUpdatedAt(),
                Set.<ImageResponse>of()
        ));
    }

    private record RouteSourceRefs(UUID tripRouteId, UUID routePlaceId) {
    }
}
