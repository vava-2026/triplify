package com.triplify.application.usecase.country.dto;

import com.triplify.domain.pagination.PageRequest;

public record GetCountriesRequest(
        PageRequest pageRequest,
        Filter filter
) {

    public GetCountriesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            String name,
            CountryBanFilter banFilter,
            boolean hasTripOrPlace
    ) {

        public enum CountryBanFilter {
            ALL,
            ONLY_BANNED,
            ONLY_UNBANNED
        }

        public Filter {
            name = name == null ? null : name.trim();
            banFilter = banFilter == null ? CountryBanFilter.ALL : banFilter;
        }
    }
}
