package com.triplify.application.usecase.tag.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import com.triplify.domain.pagination.PageRequest;

import jakarta.validation.constraints.Size;

public record GetTagsRequest(
        PageRequest pageRequest,
        Filter filter
) {

    public GetTagsRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
        filter = filter == null ? new Filter(null) : filter;
    }

    public record Filter(
            @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
            String name
    ) {

        public Filter {
            name = name == null ? null : name.trim();
        }
    }

    public static GetTagsRequest defaultRequest() {
        return new GetTagsRequest(PageRequest.defaultRequest(), null);
    }
}
