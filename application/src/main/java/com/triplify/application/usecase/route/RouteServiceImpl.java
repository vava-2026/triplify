package com.triplify.application.usecase.route;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.route.dto.AddPlaceToRouteRequest;
import com.triplify.application.usecase.route.dto.AddRouteRequest;
import com.triplify.application.usecase.route.dto.DeletePlaceFromRouteRequest;
import com.triplify.application.usecase.route.dto.DeleteRouteRequest;
import com.triplify.application.usecase.route.dto.GetRouteByIdRequest;
import com.triplify.application.usecase.route.dto.GetRoutesRequest;
import com.triplify.application.usecase.route.dto.RearrangePlacesInRouteRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.route.dto.UpdateRouteRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class RouteServiceImpl implements RouteService {

    @Override
    public Result<RouteResponse> addRoute(AddRouteRequest request) {
        // TODO: implement route creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: RouteService.addRoute"));
    }

    @Override
    public Result<RouteResponse> updateRoute(UpdateRouteRequest request) {
        // TODO: implement route update.
        return Result.fail(new ApplicationError.Unexpected("TODO: RouteService.updateRoute"));
    }

    @Override
    public Result<Void> deleteRoute(DeleteRouteRequest request) {
        // TODO: implement route deletion.
        return Result.fail(new ApplicationError.Unexpected("TODO: RouteService.deleteRoute"));
    }

    @Override
    public Result<RouteResponse> addPlaceToRoute(AddPlaceToRouteRequest request) {
        // TODO: implement adding place to route.
        return Result.fail(new ApplicationError.Unexpected("TODO: RouteService.addPlaceToRoute"));
    }

    @Override
    public Result<RouteResponse> deletePlaceFromRoute(DeletePlaceFromRouteRequest request) {
        // TODO: implement removing place from route.
        return Result.fail(new ApplicationError.Unexpected("TODO: RouteService.deletePlaceFromRoute"));
    }

    @Override
    public Result<RouteResponse> rearrangePlacesInRoute(RearrangePlacesInRouteRequest request) {
        // TODO: implement route place reordering.
        return Result.fail(new ApplicationError.Unexpected("TODO: RouteService.rearrangePlacesInRoute"));
    }

    @Override
    public Result<RouteResponse> getRouteById(GetRouteByIdRequest request) {
        // TODO: implement route retrieval by id.
        return Result.fail(new ApplicationError.Unexpected("TODO: RouteService.getRouteById"));
    }

    @Override
    public Result<Page<RouteResponse>> getRoutes(GetRoutesRequest request) {
        // TODO: implement route search with pagination and filters.
        return Result.fail(new ApplicationError.Unexpected("TODO: RouteService.getRoutes"));
    }
}
