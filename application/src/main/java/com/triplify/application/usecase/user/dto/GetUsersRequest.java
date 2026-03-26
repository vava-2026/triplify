package com.triplify.application.usecase.user.dto;

import com.triplify.domain.pagination.PageRequest;

public record GetUsersRequest(
        PageRequest pageRequest,
        Filter filter,
        OrderBy orderBy
) {

    public GetUsersRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
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
