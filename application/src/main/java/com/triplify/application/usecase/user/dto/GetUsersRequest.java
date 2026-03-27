package com.triplify.application.usecase.user.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.constraints.Size;

public record GetUsersRequest(
        PageRequest pageRequest,
        Filter filter,
        OrderBy orderBy
) {

    public GetUsersRequest {
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

    public record OrderBy(
            boolean creationTimeAsc
    ) {
    }
}
