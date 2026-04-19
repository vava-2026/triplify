package com.triplify.application.usecase.place;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.country.dto.GetCountryByIdRequest;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.AddPlaceRequest;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.place.dto.UpdatePlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlaceDetailsRequest;
import com.triplify.application.usecase.place.dto.PlaceDetailsResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.domain.error.PlaceError;
import com.triplify.domain.model.Place;
import com.triplify.domain.model.Route;
import com.triplify.domain.model.RoutePlace;
import com.triplify.domain.model.TripPlace;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.RoutePlaceRepository;
import com.triplify.domain.repository.RouteRepository;
import com.triplify.domain.repository.TripPlaceRepository;
import com.triplify.domain.repository.PlaceRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Authenticated
public class PlaceServiceImpl implements PlaceService {
    private final Logger log = LoggerFactory.getLogger(PlaceServiceImpl.class);
    private final static String DEFAULT_IMAGE_DESCRIPTION = "Cover image for place ";
    private static final int ASSOCIATED_TRIPS_LIMIT = 8;
    private static final int ASSOCIATED_ROUTES_LIMIT = 8;

    private final UserSessionContext userSessionContext;
    private final PlaceRepository placeRepository;
    private final CountryService countryService;
    private final ImageService imageService;
    private final TripService tripService;
    private final RouteRepository routeRepository;
    private final RoutePlaceRepository routePlaceRepository;
    private final TripPlaceRepository tripPlaceRepository;

    @Inject
    PlaceServiceImpl(
            PlaceRepository placeRepository,
            UserSessionContext userSessionContext,
            ImageService imageService,
            CountryService countryService,
            TripService tripService,
            RouteRepository routeRepository,
            RoutePlaceRepository routePlaceRepository,
            TripPlaceRepository tripPlaceRepository
    ) {
        this.placeRepository = placeRepository;
        this.userSessionContext = userSessionContext;
        this.imageService = imageService;
        this.countryService = countryService;
        this.tripService = tripService;
        this.routeRepository = routeRepository;
        this.routePlaceRepository = routePlaceRepository;
        this.tripPlaceRepository = tripPlaceRepository;
    }

    @Override
    @Authenticated
    public Result<PlaceResponse> addPlace(AddPlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();

        CountryResponse countryResponse = countryService.getCountryById(new GetCountryByIdRequest(request.countryId())).orThrow();
        ImageResponse image = null;
        if (request.coverImage() != null) {
            image = imageService.addImage(new AddImageRequest(request.coverImage(), DEFAULT_IMAGE_DESCRIPTION + request.title())).orThrow();
        }

        UUID imageId = image != null ? UUID.fromString(image.id()) : null;
        Place place = new Place(user.userId(), UUID.fromString(countryResponse.id()), imageId, request.title(), request.description(), request.latitude(), request.longitude());
        placeRepository.create(place);
        log.info("Added new place with id='{}', title='{}' by userId='{}'", place.getId(), place.getTitle(), user.userId());

        return Result.ok(PlaceResponse.from(place));
    }

    @Override
    public Result<PlaceResponse> updatePlace(UpdatePlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();

        var oldRes = placeRepository.findById(request.id());
        if (oldRes.isEmpty()) {
            log.warn("Attempt to update non-existing place with id='{}' by userId='{}'", request.id(), user.userId());
            return Result.fail(new PlaceError.NotFound("Place with id '{}" + request.id() + "' not found"));
        }
        Place old = oldRes.get();

        if (!old.getUserId().equals(user.userId())) {
            log.warn("Attempted to update place not created by userId='{}' by userId='{}', placeTitle='{}'", old.getUserId(), user.userId(), old.getTitle());
            return Result.fail(new PlaceError.NotOwner("Place with id '" + request.id() + "' is not owned by user"));
        }

        CountryResponse countryResponse = countryService.getCountryById(new GetCountryByIdRequest(request.countryId())).orThrow();
        UUID imageId = null;
        if (old.getCoverImage() != null && !old.getCoverImage().getUrl().equals(request.coverImage())){
            imageService.deleteImage(new DeleteImageRequest(old.getCoverImage().getId().toString())).orThrow();

            if (request.coverImage() != null) {
                var imageResult = imageService.addImage(new AddImageRequest(request.coverImage(), DEFAULT_IMAGE_DESCRIPTION + request.title()));
                imageId = UUID.fromString(imageResult.orThrow().id());
            }
            else {
                imageId = old.getCoverImage().getId();
            }
        }

        Place place = new Place(UUID.fromString(request.id()), user.userId(), UUID.fromString(countryResponse.id()), imageId, request.title(), request.description(), request.latitude(), request.longitude());
        placeRepository.update(place);

        log.info("Updated new place with id='{}', title='{}' by userId='{}'", place.getId(), place.getTitle(), user.userId());
        return Result.ok(PlaceResponse.from(place));
    }

    @Override
    public Result<Void> deletePlace(DeletePlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();

        var placeRes = placeRepository.findById(request.id());
        if (placeRes.isEmpty()) {
            log.warn("Attempt to delete non-existing place with id='{}'", request.id());
            return Result.fail(new PlaceError.NotFound("Place with id '" + request.id() + "' not found"));
        }

        Place place = placeRes.get();
        if (!place.getUserId().equals(user.userId())) {
            log.warn("Attempted to delete place not created by userId='{}' by userId='{}', placeTitle='{}'", place.getUserId(), user.userId(), place.getTitle());
            return Result.fail(new PlaceError.NotOwner("Place with id '" + request.id() + "' is not owned by user"));
        }

        placeRepository.delete(placeRes.get());
        return Result.ok();
    }

    @Override
    public Result<PlaceResponse> getPlaceById(GetPlaceByIdRequest request) {
        var placeRes = placeRepository.findById(request.id());
        if (placeRes.isEmpty()) {
            log.warn("Attempt to get non-existing place with id='{}'", request.id());
            return Result.fail(new PlaceError.NotFound("Place with id '" + request.id() + "' not found"));
        }
        return Result.ok(PlaceResponse.from(placeRes.get()));
    }

    @Override
    public Result<PlaceDetailsResponse> getPlaceDetails(GetPlaceDetailsRequest request) {
        PlaceResponse place = getPlaceById(new GetPlaceByIdRequest(request.placeId())).orThrow();

        return Result.ok(new PlaceDetailsResponse(
                place,
                loadAssociatedTrips(place.id()),
                loadAssociatedRoutes(place.id()),
                List.of()
        ));
    }

    @Override
    public Result<Page<PlaceResponse>> getPlaces(GetPlacesRequest request) {
        Page<Place> placesPage = placeRepository.findList(request.pageRequest(), request.filter());
        Page<PlaceResponse> responsePage = placesPage.map(PlaceResponse::from);
        return Result.ok(responsePage);
    }

    private List<TripResponse> loadAssociatedTrips(String placeId) {
        List<TripPlace> relatedTripPlaces = tripPlaceRepository.findByPlaceId(placeId);
        if (relatedTripPlaces.isEmpty()) {
            return List.of();
        }

        Set<String> tripIds = new LinkedHashSet<>();
        for (TripPlace tripPlace : relatedTripPlaces) {
            tripIds.add(tripPlace.getTripId().toString());
        }

        List<com.triplify.application.usecase.trip.dto.TripResponse> trips = new ArrayList<>();
        for (String tripId : tripIds) {
            var result = tripService.getTripById(new GetTripByIdRequest(tripId));
            if (result.isFailure()) {
                continue;
            }
            trips.add(result.getValue());
        }

        return trips.stream()
                .sorted(Comparator.comparing(
                        com.triplify.application.usecase.trip.dto.TripResponse::updatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(ASSOCIATED_TRIPS_LIMIT)
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

