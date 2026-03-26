package com.triplify.application.usecase.triproute;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.triproute.dto.AddTripRouteRequest;
import com.triplify.application.usecase.triproute.dto.DeleteTripRouteRequest;
import com.triplify.application.usecase.triproute.dto.GetTripRouteByIdRequest;
import com.triplify.application.usecase.triproute.dto.GetTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.RearrangeTripRoutesRequest;
import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.application.usecase.triproute.dto.UpdateTripRouteRequest;
import com.triplify.application.usecase.triproute.dto.UpdateTripRouteStatusRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class TripRouteServiceImpl implements TripRouteService {

    @Override
    public Result<TripRouteResponse> addTripRoute(AddTripRouteRequest request) {
        // TODO: implement trip route creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripRouteService.addTripRoute"));
    }

    @Override
    public Result<TripRouteResponse> updateTripRoute(UpdateTripRouteRequest request) {
        // TODO: implement trip route update.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripRouteService.updateTripRoute"));
    }

    @Override
    public Result<Void> deleteTripRoute(DeleteTripRouteRequest request) {
        // TODO: implement trip route deletion.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripRouteService.deleteTripRoute"));
    }

    @Override
    public Result<TripRouteResponse> rearrangeTripRoutes(RearrangeTripRoutesRequest request) {
        // TODO: implement trip route reordering inside a trip.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripRouteService.rearrangeTripRoutes"));
    }

    @Override
    public Result<TripRouteResponse> updateStatus(UpdateTripRouteStatusRequest request) {
        // TODO: implement trip route status update.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripRouteService.updateStatus"));
    }

    @Override
    public Result<TripRouteResponse> getTripRouteById(GetTripRouteByIdRequest request) {
        // TODO: implement trip route retrieval by id.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripRouteService.getTripRouteById"));
    }

    @Override
    public Result<Page<TripRouteResponse>> getTripRoutes(GetTripRoutesRequest request) {
        // TODO: implement trip route search with pagination and filters.
        return Result.fail(new ApplicationError.Unexpected("TODO: TripRouteService.getTripRoutes"));
    }
}

