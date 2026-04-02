package com.triplify.application.usecase.triproute.dto;

import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;

public record GetTripRoutesRequest(
        PageRequest pageRequest,
        Filter filter
) {

    public GetTripRoutesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            String tripId,
            StatusEnum status
    ) {

        public Filter {
            tripId = tripId == null ? null : tripId.trim();
        }
    }
}
