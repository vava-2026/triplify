package com.triplify.application.usecase.place;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.place.dto.AddPlaceRequest;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.place.dto.UpdatePlaceRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class PlaceServiceImpl implements PlaceService {

    @Override
    public Result<PlaceResponse> addPlace(AddPlaceRequest request) {
        // TODO: implement place creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: PlaceService.addPlace"));
    }

    @Override
    public Result<PlaceResponse> updatePlace(UpdatePlaceRequest request) {
        // TODO: implement place update.
        return Result.fail(new ApplicationError.Unexpected("TODO: PlaceService.updatePlace"));
    }

    @Override
    public Result<Void> deletePlace(DeletePlaceRequest request) {
        // TODO: implement place deletion.
        return Result.fail(new ApplicationError.Unexpected("TODO: PlaceService.deletePlace"));
    }

    @Override
    public Result<PlaceResponse> getPlaceById(GetPlaceByIdRequest request) {
        // TODO: implement place retrieval by id.
        return Result.fail(new ApplicationError.Unexpected("TODO: PlaceService.getPlaceById"));
    }

    @Override
    public Result<Page<PlaceResponse>> getPlaces(GetPlacesRequest request) {
        // TODO: implement place search with pagination and filters.
        return Result.fail(new ApplicationError.Unexpected("TODO: PlaceService.getPlaces"));
    }
}

