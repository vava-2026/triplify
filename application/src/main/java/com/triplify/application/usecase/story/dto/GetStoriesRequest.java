package com.triplify.application.usecase.story.dto;

import com.triplify.domain.pagination.PageRequest;

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
            String userId,
            String tripId,
            String tripRouteId,
            String tripPlaceId,
            String title,
            Instant storyTimeFrom,
            Instant storyTimeTo
    ) {

        public Filter {
            userId = userId == null ? null : userId.trim();
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
