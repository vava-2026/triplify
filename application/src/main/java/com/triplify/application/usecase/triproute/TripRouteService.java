package com.triplify.application.usecase.triproute;

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

public interface TripRouteService {

    Result<TripRouteResponse> addTripRoute(AddTripRouteRequest request);

    Result<TripRouteResponse> updateTripRoute(UpdateTripRouteRequest request);

    Result<Void> deleteTripRoute(DeleteTripRouteRequest request);

    Result<TripRouteResponse> rearrangeTripRoutes(RearrangeTripRoutesRequest request);

    Result<TripRouteResponse> updateStatus(UpdateTripRouteStatusRequest request);

    Result<TripRouteResponse> getTripRouteById(GetTripRouteByIdRequest request);

    Result<Page<TripRouteResponse>> getTripRoutes(GetTripRoutesRequest request);
}
