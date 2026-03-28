package com.triplify.application.usecase.tripplace.dto;

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
            Instant visitFrom,
            Instant visitTo
    ) {

        public Filter {
            tripId = tripId == null ? null : tripId.trim();
        }
    }

    public record OrderBy(
            boolean visitTimeAsc
    ) {
    }
}
