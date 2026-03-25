package com.triplify.application.usecase.tag.dto;

import com.triplify.domain.pagination.PageRequest;

public record GetTagsRequest(
        PageRequest pageRequest,
        Filter filter
) {

    public GetTagsRequest {
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
