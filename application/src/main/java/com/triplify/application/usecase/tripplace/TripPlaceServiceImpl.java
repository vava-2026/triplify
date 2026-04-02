package com.triplify.application.usecase.tripplace;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.tripplace.dto.AddTripPlaceRequest;
import com.triplify.application.usecase.tripplace.dto.DeleteTripPlaceRequest;
import com.triplify.application.usecase.tripplace.dto.GetTripPlaceByIdRequest;
import com.triplify.application.usecase.tripplace.dto.GetTripPlacesRequest;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.tripplace.dto.UpdateTripPlaceRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class TripPlaceServiceImpl implements TripPlaceService {

    @Override
    public Result<TripPlaceResponse> addTripPlace(AddTripPlaceRequest request) {
        // TODO: implement trip place creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripPlaceService.addTripPlace"));
    }

    @Override
    public Result<TripPlaceResponse> updateTripPlace(UpdateTripPlaceRequest request) {
        // TODO: implement trip place update.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripPlaceService.updateTripPlace"));
    }

    @Override
    public Result<Void> deleteTripPlace(DeleteTripPlaceRequest request) {
        // TODO: implement trip place deletion.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripPlaceService.deleteTripPlace"));
    }

    @Override
    public Result<TripPlaceResponse> getTripPlaceById(GetTripPlaceByIdRequest request) {
        // TODO: implement trip place retrieval by id.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripPlaceService.getTripPlaceById"));
    }

    @Override
    public Result<Page<TripPlaceResponse>> getTripPlaces(GetTripPlacesRequest request) {
        // TODO: implement trip place search with pagination, filters and ordering.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripPlaceService.getTripPlaces"));
    }
}
