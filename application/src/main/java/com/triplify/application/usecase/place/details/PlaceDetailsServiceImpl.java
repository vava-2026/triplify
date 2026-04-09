package com.triplify.application.usecase.place.details;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.place.details.dto.GetPlaceDetailsRequest;
import com.triplify.application.usecase.place.details.dto.PlaceDetailsResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.model.Route;
import com.triplify.domain.model.RoutePlace;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.PlaceRepository;
import com.triplify.domain.repository.RoutePlaceRepository;
import com.triplify.domain.repository.RouteRepository;
import com.triplify.domain.result.Result;
import com.triplify.application.usecase.place.PlaceService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Authenticated
public class PlaceDetailsServiceImpl implements PlaceDetailsService {

    private static final int ASSOCIATED_PLACES_LIMIT = 8;
    private static final int ASSOCIATED_ROUTES_LIMIT = 8;

    private final PlaceService placeService;
    private final PlaceRepository placeRepository;
    private final RouteRepository routeRepository;
    private final RoutePlaceRepository routePlaceRepository;

    @Inject
    PlaceDetailsServiceImpl(
            PlaceService placeService,
            PlaceRepository placeRepository,
            RouteRepository routeRepository,
            RoutePlaceRepository routePlaceRepository
    ) {
        this.placeService = placeService;
        this.placeRepository = placeRepository;
        this.routeRepository = routeRepository;
        this.routePlaceRepository = routePlaceRepository;
    }

    @Override
    public Result<PlaceDetailsResponse> getPlaceDetails(GetPlaceDetailsRequest request) {
        PlaceResponse place = placeService.getPlaceById(new GetPlaceByIdRequest(request.placeId())).orThrow();

        return Result.ok(new PlaceDetailsResponse(
                place,
                loadAssociatedPlaces(place),
                loadAssociatedRoutes(place.id()),
                List.of()
        ));
    }

    private List<PlaceResponse> loadAssociatedPlaces(PlaceResponse place) {
        if (place.country() == null || place.country().id() == null || place.country().id().isBlank()) {
            return List.of();
        }

        return placeRepository.findList(
                        new PageRequest(0, ASSOCIATED_PLACES_LIMIT + 1),
                        new PlaceFilter(null, place.country().id())
                ).items().stream()
                .map(PlaceResponse::from)
                .filter(candidate -> !place.id().equals(candidate.id()))
                .limit(ASSOCIATED_PLACES_LIMIT)
                .toList();
    }

    private List<RouteResponse> loadAssociatedRoutes(String placeId) {
        List<RoutePlace> relatedRoutePlaces = routePlaceRepository.findByPlaceId(placeId);
        if (relatedRoutePlaces.isEmpty()) {
            return List.of();
        }

        Set<String> routeIds = new LinkedHashSet<>();
        for (RoutePlace routePlace : relatedRoutePlaces) {
            routeIds.add(routePlace.getRouteId().toString());
        }

        List<RouteResponse> routes = new ArrayList<>();
        for (String routeId : routeIds) {
            Route route = routeRepository.findById(routeId).orElse(null);
            if (route == null) {
                continue;
            }

            List<RoutePlace> routePlaces = routePlaceRepository.findByRouteId(routeId);
            routes.add(RouteResponse.from(route, routePlaces));
        }

        return routes.stream()
                .sorted(Comparator.comparing(RouteResponse::updatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(ASSOCIATED_ROUTES_LIMIT)
                .toList();
    }
}
