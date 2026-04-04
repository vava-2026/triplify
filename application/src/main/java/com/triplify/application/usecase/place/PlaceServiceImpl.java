package com.triplify.application.usecase.place;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.country.dto.GetCountryByIdRequest;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.GetImageByIdRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.AddPlaceRequest;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.place.dto.UpdatePlaceRequest;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.error.PlaceError;
import com.triplify.domain.model.Place;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.PlaceRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Authenticated
public class PlaceServiceImpl implements PlaceService {
    private final Logger log = LoggerFactory.getLogger(PlaceServiceImpl.class);
    private final static String DEFAULT_IMAGE_DESCRIPTION = "Cover image for place ";

    private final UserSessionContext userSessionContext;
    private final PlaceRepository placeRepository;
    private final CountryService countryService;
    private final ImageService imageService;

    @Inject
    PlaceServiceImpl(PlaceRepository placeRepository, UserSessionContext userSessionContext, ImageService imageService, CountryService countryService) {
        this.placeRepository = placeRepository;
        this.userSessionContext = userSessionContext;
        this.imageService = imageService;
        this.countryService = countryService;
    }

    @Override
    @Authenticated
    public Result<PlaceResponse> addPlace(AddPlaceRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();

        Result<CountryResponse> country = countryService.getCountryById(new GetCountryByIdRequest(request.countryId()));
        if (country.isFailure()) {
            return Result.fail(country.getError());
        }
        ImageResponse image = null;
        if (request.coverImage() != null) {
            var imageResult = imageService.addImage(new AddImageRequest(request.coverImage(), DEFAULT_IMAGE_DESCRIPTION + request.title()));
            if (imageResult.isFailure()) {
                return Result.fail(imageResult.getError());
            }
            image = imageResult.getValue();
        }

        UUID imageId = image != null ? UUID.fromString(image.id()) : null;
        Place place = new Place(user.userId(), UUID.fromString(country.getValue().id()), imageId, request.title(), request.description(), request.latitude(), request.longitude());
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

        Result<CountryResponse> country = countryService.getCountryById(new GetCountryByIdRequest(request.countryId()));
        if (country.isFailure()) {
            return Result.fail(country.getError());
        }
        UUID imageId = null;
        if (old.getCoverImage() != null && !old.getCoverImage().getUrl().equals(request.coverImage())){
            var deleteImageRes = imageService.deleteImage(new DeleteImageRequest(old.getCoverImage().getId().toString()));
            if (deleteImageRes.isFailure()) {
                return Result.fail(deleteImageRes.getError());
            }

            if (request.coverImage() != null) {
                var imageResult = imageService.addImage(new AddImageRequest(request.coverImage(), DEFAULT_IMAGE_DESCRIPTION + request.title()));
                if (imageResult.isFailure()) {
                    return Result.fail(imageResult.getError());
                }
                imageId = UUID.fromString(imageResult.getValue().id());
            }
            else {
                imageId = old.getCoverImage().getId();
            }
        }

        Place place = new Place(UUID.fromString(request.id()), user.userId(), UUID.fromString(country.getValue().id()), imageId, request.title(), request.description(), request.latitude(), request.longitude());
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
    public Result<Page<PlaceResponse>> getPlaces(GetPlacesRequest request) {
        Page<Place> placesPage = placeRepository.findList(request.pageRequest(), request.filter());
        Page<PlaceResponse> responsePage = placesPage.map(PlaceResponse::from);
        return Result.ok(responsePage);
    }
}

