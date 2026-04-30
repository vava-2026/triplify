package com.triplify.application.usecase.image.dto;

import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.model.enums.ImageOwnerType;

import java.time.Instant;

public record GetImagesRequest(
        PageRequest pageRequest,
        Filter filter,
        OrderBy orderBy
) {

    public GetImagesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
        filter = filter == null ? new Filter(null, null, null, null) : filter;
        orderBy = orderBy == null ? new OrderBy(true) : orderBy;
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
