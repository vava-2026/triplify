package com.triplify.application.usecase.place.dto;

import com.triplify.domain.pagination.PageRequest;

public record GetPlacesRequest(
        PageRequest pageRequest,
        Filter filter
) {

    public GetPlacesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            String userId,
            String name,
            String countryId
    ) {

        public Filter {
            userId = userId == null ? null : userId.trim();
            name = name == null ? null : name.trim();
            countryId = countryId == null ? null : countryId.trim();
        }
    }
}
