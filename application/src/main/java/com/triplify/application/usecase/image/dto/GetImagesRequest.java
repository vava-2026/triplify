package com.triplify.application.usecase.image.dto;

import com.triplify.domain.pagination.PageRequest;

import java.time.Instant;

public record GetImagesRequest(
        PageRequest pageRequest,
        Filter filter,
        OrderBy orderBy
) {

    public GetImagesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            String ownerId,
            ImageOwnerType ownerType,
            Instant uploadedFrom,
            Instant uploadedTo
    ) {

        public Filter {
            ownerId = ownerId == null ? null : ownerId.trim();
        }
    }

    public record OrderBy(
            boolean uploadTimeAsc
    ) {
    }
}
