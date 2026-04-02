package com.triplify.application.usecase.country.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import com.triplify.domain.filter.CountryFilter;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.constraints.Size;

public record GetCountriesRequest(
        PageRequest pageRequest,
        CountryFilter filter
) {

    public GetCountriesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
        if (filter.name().length() > DtoConstraints.NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(ValidationMessage.Constants.NAME_TOO_LONG);
        }
    }
}
