package com.triplify.application.usecase.tripplace.dto;

import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.pagination.PageRequest;

import java.time.Instant;

public record GetTripPlacesRequest(
        PageRequest pageRequest,
        Filter filter,
        OrderBy orderBy
) {

    public GetTripPlacesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            String tripId,
            TripPlaceSourceType sourceType,
            String tripRouteId,
            String routePlaceId,
            Instant visitFrom,
            Instant visitTo
    ) {

        public Filter {
            tripId = tripId == null ? null : tripId.trim();
            tripRouteId = tripRouteId == null ? null : tripRouteId.trim();
            routePlaceId = routePlaceId == null ? null : routePlaceId.trim();
        }
    }

    public record OrderBy(
            boolean visitTimeAsc
    ) {
    }
}
