package com.triplify.application.usecase.trip;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.trip.dto.AddTripRequest;
import com.triplify.application.usecase.trip.dto.DeleteTripRequest;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.GetTripsRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.trip.dto.UpdateTripRequest;
import com.triplify.application.usecase.trip.dto.UpdateTripStatusRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class TripServiceImpl implements TripService {

    @Override
    public Result<TripResponse> addTrip(AddTripRequest request) {
        // TODO: implement trip creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripService.addTrip"));
    }

    @Override
    public Result<TripResponse> updateTrip(UpdateTripRequest request) {
        // TODO: implement trip update.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripService.updateTrip"));
    }

    @Override
    public Result<Void> deleteTrip(DeleteTripRequest request) {
        // TODO: implement trip deletion.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripService.deleteTrip"));
    }

    @Override
    public Result<TripResponse> updateStatus(UpdateTripStatusRequest request) {
        // TODO: implement trip status update.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripService.updateStatus"));
    }

    @Override
    public Result<TripResponse> getTripById(GetTripByIdRequest request) {
        // TODO: implement trip retrieval by id.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripService.getTripById"));
    }

    @Override
    public Result<Page<TripResponse>> getTrips(GetTripsRequest request) {
        // TODO: implement trip search with pagination, filters and ordering.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripService.getTrips"));
    }
}

