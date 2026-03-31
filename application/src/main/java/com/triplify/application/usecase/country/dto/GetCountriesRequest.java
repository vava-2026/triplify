package com.triplify.application.usecase.country.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.constraints.Size;

public record GetCountriesRequest(
        PageRequest pageRequest,
        Filter filter
) {

    public GetCountriesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
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
