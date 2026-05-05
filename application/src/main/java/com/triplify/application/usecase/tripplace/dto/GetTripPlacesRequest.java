package com.triplify.application.usecase.tripplace.dto;

import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.pagination.PageRequest;

import java.time.Instant;
import java.util.UUID;

public record GetTripPlacesRequest(
        PageRequest pageRequest,
        Filter filter,
        OrderBy orderBy
) {

    public GetTripPlacesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            UUID tripId,
            TripPlaceSourceType sourceType,
            UUID tripRouteId,
            UUID routePlaceId,
            Instant visitFrom,
            Instant visitTo
    ) {
    }

    public record OrderBy(
            boolean visitTimeAsc
    ) {
    }
}
