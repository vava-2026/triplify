package com.triplify.application.usecase.story.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record GetStoriesRequest(
        PageRequest pageRequest,
        Filter filter,
        OrderBy orderBy
) {

    public GetStoriesRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }

    public record Filter(
            String tripId,
            String tripRouteId,
            String tripPlaceId,
            @Size(max = DtoConstraints.TITLE_MAX_LENGTH, message = ValidationMessage.Constants.TITLE_TOO_LONG)
            String title,
            Instant storyTimeFrom,
            Instant storyTimeTo
    ) {

        public Filter {
            tripId = tripId == null ? null : tripId.trim();
            tripRouteId = tripRouteId == null ? null : tripRouteId.trim();
            tripPlaceId = tripPlaceId == null ? null : tripPlaceId.trim();
            title = title == null ? null : title.trim();
        }
    }

    public record OrderBy(
            boolean storyTimeAsc
    ) {
    }
}
