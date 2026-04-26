package com.triplify.application.usecase.place;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.country.dto.GetCountryByIdRequest;
import com.triplify.application.shared.ColorTheme;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.AddPlaceRequest;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.dto.GetPlaceRoutesRequest;
import com.triplify.application.usecase.place.dto.GetPlaceTripsRequest;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.place.dto.UpdatePlaceRequest;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.domain.model.Category;
import com.triplify.domain.error.PlaceError;
import com.triplify.domain.model.Place;
import com.triplify.domain.model.RouteWithPlaces;
import com.triplify.domain.model.Trip;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.RoutePlaceRepository;
import com.triplify.domain.repository.TripPlaceRepository;
import com.triplify.domain.repository.PlaceRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Authenticated
public class PlaceServiceImpl implements PlaceService {
    private final Logger log = LoggerFactory.getLogger(PlaceServiceImpl.class);
    private final static String DEFAULT_IMAGE_DESCRIPTION = "Cover image for place ";

    private final UserSessionContext userSessionContext;
    private final PlaceRepository placeRepository;
    private final CountryService countryService;
    private final ImageService imageService;
    private final RoutePlaceRepository routePlaceRepository;
    private final TripPlaceRepository tripPlaceRepository;

    @Inject
    PlaceServiceImpl(
            PlaceRepository placeRepository,
            UserSessionContext userSessionContext,
            ImageService imageService,
            CountryService countryService,
            RoutePlaceRepository routePlaceRepository,
            TripPlaceRepository tripPlaceRepository
    ) {
        this.placeRepository = placeRepository;
        this.userSessionContext = userSessionContext;
        this.imageService = imageService;
        this.countryService = countryService;
        this.routePlaceRepository = routePlaceRepository;
        this.tripPlaceRepository = tripPlaceRepository;
    }

    @Override
    @Authenticated
    public Result<PlaceResponse> addPlace(AddPlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        log.info("Adding new place with title='{}' by userId='{}'", request.title(), user.userId());

        CountryResponse countryResponse = countryService.getCountryById(new GetCountryByIdRequest(request.countryId())).orThrow();
        ImageResponse image = null;
        if (request.coverImage() != null) {
            image = imageService.addImage(new AddImageRequest(request.coverImage(), DEFAULT_IMAGE_DESCRIPTION + request.title())).orThrow();
        }

        UUID imageId = image != null ? image.id() : null;
        Place place = new Place(user.userId(), countryResponse.id(), imageId, request.title(), request.description(), request.latitude(), request.longitude());
        placeRepository.create(place);
        log.info("Added new place with id='{}', title='{}' by userId='{}'", place.getId(), place.getTitle(), user.userId());

        return Result.ok(PlaceResponse.from(place));
    }

    @Override
    public Result<PlaceResponse> updatePlace(UpdatePlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        log.info("Updating new place with id='{}', title='{}' by userId='{}'", request.id(), request.title(), user.userId());

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
            imageService.deleteImage(new DeleteImageRequest(old.getCoverImage().getId())).orThrow();

            if (request.coverImage() != null) {
                var imageResult = imageService.addImage(new AddImageRequest(request.coverImage(), DEFAULT_IMAGE_DESCRIPTION + request.title()));
                imageId = imageResult.orThrow().id();
            }
            else {
                imageId = old.getCoverImage().getId();
            }
        }

        Place place = new Place(request.id(), user.userId(), countryResponse.id(), imageId, request.title(), request.description(), request.latitude(), request.longitude());
        placeRepository.update(place);

        log.info("Updated new place with id='{}', title='{}' by userId='{}'", place.getId(), place.getTitle(), user.userId());
        return Result.ok(PlaceResponse.from(place));
    }

    @Override
    public Result<Void> deletePlace(DeletePlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        log.info("Deleting place with id='{}' by userId='{}'", request.id(), user.userId());

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

        if (place.getCoverImage() != null) {
            imageService.deleteImage(new DeleteImageRequest(place.getCoverImage().getId())).orThrow();
        }

        placeRepository.delete(placeRes.get());
        return Result.ok();
    }

    @Override
    public Result<PlaceResponse> getPlaceById(GetPlaceByIdRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        log.info("Getting place with id='{}' by userId='{}'", request.id(), user.userId());
        Result<Place> place = requireOwnedPlace(request.id(), user.userId());
        if (place.isFailure()) {
            log.warn("Attempt to get non-existing place with id='{}'", request.id());
            return Result.fail(new PlaceError.NotFound("Place with id '" + request.id() + "' not found"));
        }
        return Result.ok(PlaceResponse.from(place.getValue()));
    }

    @Override
    public Result<Page<TripResponse>> getPlaceTrips(GetPlaceTripsRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        requireOwnedPlace(request.placeId(), user.userId()).orThrow();

        Page<Trip> tripPage = tripPlaceRepository.findTripsByPlaceId(request.pageRequest(), request.placeId(), user.userId());
        List<TripResponse> trips = tripPage.items().stream().map(this::toTripResponse).toList();
        return Result.ok(Page.of(trips, request.pageRequest(), tripPage.hasNext()));
    }

    @Override
    public Result<Page<RouteResponse>> getPlaceRoutes(GetPlaceRoutesRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        requireOwnedPlace(request.placeId(), user.userId()).orThrow();

        Page<RouteWithPlaces> routePage = routePlaceRepository.findRoutesWithPlacesByPlaceId(
                request.pageRequest(),
                request.placeId(),
                user.userId()
        );
        List<RouteResponse> routes = routePage.items().stream()
                .map(item -> RouteResponse.from(item.route(), item.routePlaces()))
                .toList();

        return Result.ok(Page.of(routes, request.pageRequest(), routePage.hasNext()));
    }

    @Override
    public Result<Page<PlaceResponse>> getPlaces(GetPlacesRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Page<Place> placesPage = placeRepository.findList(request.pageRequest(), request.filter(), user.userId());
        Page<PlaceResponse> responsePage = placesPage.map(PlaceResponse::from);
        return Result.ok(responsePage);
    }

    private Result<Place> requireOwnedPlace(UUID placeId, UUID userId) {
        var placeRes = placeRepository.findById(placeId);
        if (placeRes.isEmpty() || !placeRes.get().getUserId().equals(userId)) {
            return Result.fail(new PlaceError.NotFound("Place with id '" + placeId + "' not found"));
        }
        return Result.ok(placeRes.get());
    }

    private TripResponse toTripResponse(Trip trip) {
        ImageResponse coverImage = trip.getCoverImage() == null ? null : ImageResponse.from(trip.getCoverImage());

        Category category = trip.getCategory();
        CategoryResponse categoryResponse = category == null ? null : new CategoryResponse(
                category.getId(),
                category.getCreatedById(),
                category.getName(),
                category.getNameSk(),
                category.getDescription(),
                category.getDescriptionSk(),
                category.getEmojiUnicode(),
            category.getColor() == null ? null : ColorTheme.from(category.getColor())
        );

        Set<TagResponse> tagResponses = trip.getTags().stream()
            .map(tag -> new TagResponse(
                tag.getId(),
                tag.getUserId(),
                tag.getName(),
                tag.getColor() == null ? null : ColorTheme.from(tag.getColor())
            ))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<CountryResponse> countryResponses = trip.getCountries().stream()
                .map(CountryResponse::from)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new TripResponse(
                trip.getId(),
                trip.getUserId(),
                categoryResponse,
                trip.getTitle(),
                trip.getDescription(),
                trip.getStatus(),
                trip.getStartedAt(),
                trip.getEndedAt(),
                trip.getCreatedAt(),
                trip.getUpdatedAt(),
                tagResponses,
                coverImage,
                countryResponses
        );
    }

}

