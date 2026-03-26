package com.triplify.application.usecase.story.dto;

import java.time.Instant;
import java.util.Set;

public record StoryResponse(
        String id,
        String userId,
        String tripId,
        String tripRouteId,
        String tripPlaceId,
        String emotionId,
        String title,
        String description,
        Instant storyTime,
        Instant createdAt,
        Set<String> tagIds,
        Set<String> imageIds
) {
}
