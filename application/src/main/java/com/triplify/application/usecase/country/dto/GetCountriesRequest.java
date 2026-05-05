package com.triplify.application.usecase.country.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.shared.DtoConstraints;
import com.triplify.domain.filter.CountryFilter;
import com.triplify.domain.pagination.PageRequest;

public record GetCountriesRequest(
        PageRequest pageRequest,
        CountryFilter filter
) {

    public GetCountriesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
        filter = filter == null ? new CountryFilter(null, null, false) : filter;
        String name = filter.name() == null ? null : filter.name().trim();
        if (name != null && name.length() > DtoConstraints.NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(ValidationMessage.Constants.NAME_TOO_LONG);
        }
    }
}
