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
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.application.usecase.triproute.dto.UpdateTripRouteRequest;
import com.triplify.application.usecase.triproute.dto.UpdateTripRouteStatusRequest;
import com.triplify.domain.error.RouteError;
import com.triplify.domain.error.TripError;
import com.triplify.domain.error.TripRouteError;
import com.triplify.domain.model.Trip;
import com.triplify.domain.model.TripRoute;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.RouteRepository;
import com.triplify.domain.repository.TripRepository;
import com.triplify.domain.repository.TripRouteRepository;
import com.triplify.domain.result.Result;

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

    private final TripRouteRepository tripRouteRepository;
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final RouteService routeService;
    private final UserSessionContext userSessionContext;

    @Inject
    TripRouteServiceImpl(
            TripRouteRepository tripRouteRepository,
            TripRepository tripRepository,
            RouteRepository routeRepository,
            RouteService routeService,
            UserSessionContext userSessionContext
    ) {
        this.tripRouteRepository = tripRouteRepository;
        this.tripRepository = tripRepository;
        this.routeRepository = routeRepository;
        this.routeService = routeService;
        this.userSessionContext = userSessionContext;
    }

    @Override
    public Result<TripRouteResponse> addTripRoute(AddTripRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Result<Trip> tripResult = requireOwnedTrip(request.tripId(), user.userId());
        if (tripResult.isFailure()) {
            return Result.fail(tripResult.getError());
        }

        if (routeRepository.findById(request.routeId()).isEmpty()) {
            return Result.fail(new RouteError.NotFound(request.routeId()));
        }

        var existing = tripRouteRepository.findByTripIdAndRouteId(request.tripId(), request.routeId());
        if (existing.isPresent()) {
            return getTripRouteById(new GetTripRouteByIdRequest(existing.get().getId().toString()));
        }

        TripRoute tripRoute = new TripRoute(
                UUID.fromString(request.tripId()),
                UUID.fromString(request.routeId()),
                request.order()
        );
        tripRouteRepository.create(tripRoute);
        return getTripRouteById(new GetTripRouteByIdRequest(tripRoute.getId().toString()));
    }

    @Override
    public Result<TripRouteResponse> updateTripRoute(UpdateTripRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Result<TripRoute> existingResult = requireTripRoute(request.id());
        if (existingResult.isFailure()) {
            return Result.fail(existingResult.getError());
        }

        TripRoute existing = existingResult.getValue();
        Result<Trip> tripResult = requireOwnedTrip(existing.getTripId().toString(), user.userId());
        if (tripResult.isFailure()) {
            return Result.fail(tripResult.getError());
        }

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
        return getTripRouteById(new GetTripRouteByIdRequest(updated.getId().toString()));
    }

    @Override
    public Result<Void> deleteTripRoute(DeleteTripRouteRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Result<TripRoute> existingResult = requireTripRoute(request.id());
        if (existingResult.isFailure()) {
            return Result.fail(existingResult.getError());
        }

        TripRoute existing = existingResult.getValue();
        Result<Trip> tripResult = requireOwnedTrip(existing.getTripId().toString(), user.userId());
        if (tripResult.isFailure()) {
            return Result.fail(tripResult.getError());
        }

        tripRouteRepository.delete(existing);
        resequenceTripRoutes(existing.getTripId().toString());
        return Result.ok();
    }

    @Override
    public Result<TripRouteResponse> rearrangeTripRoutes(RearrangeTripRoutesRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Result<Trip> tripResult = requireOwnedTrip(request.id(), user.userId());
        if (tripResult.isFailure()) {
            return Result.fail(tripResult.getError());
        }
        if (request.routesIdsInOrder() == null) {
            return Result.fail(new ApplicationError.Unexpected("Trip route order is required"));
        }

        Page<TripRoute> currentPage = tripRouteRepository.findList(new PageRequest(0, 512), request.id(), null);
        List<TripRoute> currentRoutes = currentPage.items();
        List<String> requestedRouteIds = new ArrayList<>(request.routesIdsInOrder());

        if (requestedRouteIds.size() != currentRoutes.size()) {
            return Result.fail(new ApplicationError.Unexpected("Trip route order does not match current routes"));
        }

        Set<String> currentRouteIds = currentRoutes.stream()
                .map(tripRoute -> tripRoute.getRouteId().toString())
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        if (!currentRouteIds.equals(new HashSet<>(requestedRouteIds))) {
            return Result.fail(new ApplicationError.Unexpected("Trip route order does not match current routes"));
        }

        for (int i = 0; i < requestedRouteIds.size(); i++) {
            String routeId = requestedRouteIds.get(i);
            TripRoute tripRoute = currentRoutes.stream()
                    .filter(item -> routeId.equals(item.getRouteId().toString()))
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
            return Result.fail(new TripRouteError.NotFound(request.id()));
        }
        return getTripRouteById(new GetTripRouteByIdRequest(reordered.get().getId().toString()));
    }

    @Override
    public Result<TripRouteResponse> updateStatus(UpdateTripRouteStatusRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Result<TripRoute> existingResult = requireTripRoute(request.id());
        if (existingResult.isFailure()) {
            return Result.fail(existingResult.getError());
        }

        TripRoute existing = existingResult.getValue();
        Result<Trip> tripResult = requireOwnedTrip(existing.getTripId().toString(), user.userId());
        if (tripResult.isFailure()) {
            return Result.fail(tripResult.getError());
        }

        Result<Void> statusResult = validateStatusTransition(existing.getStatus(), request.status());
        if (statusResult.isFailure()) {
            return Result.fail(statusResult.getError());
        }

        Instant startedAt = request.startedAt() != null ? request.startedAt() : existing.getStartedAt();
        Instant endedAt = request.endedAt() != null ? request.endedAt() : existing.getEndedAt();
        Result<Void> datesResult = validateDates(startedAt, endedAt);
        if (datesResult.isFailure()) {
            return Result.fail(datesResult.getError());
        }

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
        return getTripRouteById(new GetTripRouteByIdRequest(updated.getId().toString()));
    }

    @Override
    public Result<TripRouteResponse> getTripRouteById(GetTripRouteByIdRequest request) {
        Result<TripRoute> tripRouteResult = requireTripRoute(request.id());
        if (tripRouteResult.isFailure()) {
            return Result.fail(tripRouteResult.getError());
        }
        return toResponse(tripRouteResult.getValue());
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
            Result<TripRouteResponse> response = toResponse(tripRoute);
            if (response.isFailure()) {
                return Result.fail(response.getError());
            }
            responses.add(response.getValue());
        }

        return Result.ok(new Page<>(responses, page.page(), page.size(), page.hasNext()));
    }

    private Result<Trip> requireOwnedTrip(String tripId, UUID userId) {
        var tripRes = tripRepository.findById(tripId);
        if (tripRes.isEmpty()) {
            return Result.fail(new TripError.NotFound(tripId));
        }

        Trip trip = tripRes.get();
        if (!Objects.equals(trip.getUserId(), userId)) {
            return Result.fail(new TripError.NotOwner(tripId));
        }

        return Result.ok(trip);
    }

    private Result<TripRoute> requireTripRoute(String tripRouteId) {
        return tripRouteRepository.findById(tripRouteId)
                .<Result<TripRoute>>map(Result::ok)
                .orElseGet(() -> Result.fail(new TripRouteError.NotFound(tripRouteId)));
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
        Result<RouteResponse> routeResult = routeService.getRouteById(
                new GetRouteByIdRequest(tripRoute.getRouteId().toString())
        );
        if (routeResult.isFailure()) {
            return Result.fail(routeResult.getError());
        }

        return Result.ok(new TripRouteResponse(
                tripRoute.getId().toString(),
                tripRoute.getTripId().toString(),
                routeResult.getValue(),
                tripRoute.getOrder(),
                tripRoute.getStatus(),
                tripRoute.getStartedAt(),
                tripRoute.getEndedAt(),
                tripRoute.getCreatedAt(),
                tripRoute.getUpdatedAt(),
                Set.<ImageResponse>of()
        ));
    }

    private void resequenceTripRoutes(String tripId) {
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
}
