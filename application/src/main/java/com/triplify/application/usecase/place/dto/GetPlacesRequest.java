package com.triplify.application.usecase.place.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.pagination.PageRequest;

public record GetPlacesRequest(
        PageRequest pageRequest,
        PlaceFilter filter
) {

    public GetPlacesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
        filter = filter == null ? new PlaceFilter(null, null) : filter;
        if (filter.name() != null && filter.name().length() > DtoConstraints.NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(ValidationMessage.Constants.NAME_TOO_LONG);
        }
    }
}
