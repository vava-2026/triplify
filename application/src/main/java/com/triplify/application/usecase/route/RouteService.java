package com.triplify.application.usecase.route;

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

public interface RouteService {

    Result<RouteResponse> addRoute(AddRouteRequest request);

    Result<RouteResponse> updateRoute(UpdateRouteRequest request);

    Result<Void> deleteRoute(DeleteRouteRequest request);

    Result<RouteResponse> addPlaceToRoute(AddPlaceToRouteRequest request);

    Result<RouteResponse> deletePlaceFromRoute(DeletePlaceFromRouteRequest request);

    Result<RouteResponse> rearrangePlacesInRoute(RearrangePlacesInRouteRequest request);

    Result<RouteResponse> getRouteById(GetRouteByIdRequest request);

    Result<Page<RouteResponse>> getRoutes(GetRoutesRequest request);
}
