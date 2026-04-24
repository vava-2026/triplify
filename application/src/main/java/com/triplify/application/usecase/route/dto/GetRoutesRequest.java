package com.triplify.application.usecase.route.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.constraints.Size;

public record GetRoutesRequest(
        PageRequest pageRequest,
        Filter filter
) {

    public GetRoutesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
            String name
    ) {

        public Filter {
            name = name == null ? null : name.trim();
        }
    }
}
