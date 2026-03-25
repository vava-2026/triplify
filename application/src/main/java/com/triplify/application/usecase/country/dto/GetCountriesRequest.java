package com.triplify.application.usecase.country.dto;

import com.triplify.domain.pagination.PageRequest;

public record GetCountriesRequest(
        PageRequest pageRequest,
        String name,
        CountryBanFilter banFilter,
        boolean hasTripOrPlace
) {

    public GetCountriesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
        banFilter = banFilter == null ? CountryBanFilter.ALL : banFilter;
        name = name == null ? null : name.trim();
    }
}
