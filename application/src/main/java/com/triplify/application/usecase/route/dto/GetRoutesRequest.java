package com.triplify.application.usecase.route.dto;

import com.triplify.domain.pagination.PageRequest;

public record GetRoutesRequest(
        PageRequest pageRequest,
        Filter filter
) {

    public GetRoutesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            String name,
            String userId
    ) {

        public Filter {
            name = name == null ? null : name.trim();
            userId = userId == null ? null : userId.trim();
        }
    }
}

