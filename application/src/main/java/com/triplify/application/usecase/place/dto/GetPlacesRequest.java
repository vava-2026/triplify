package com.triplify.application.usecase.place.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.constraints.Size;

public record GetPlacesRequest(
        PageRequest pageRequest,
        Filter filter
) {

    public GetPlacesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            String userId,
            @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
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
