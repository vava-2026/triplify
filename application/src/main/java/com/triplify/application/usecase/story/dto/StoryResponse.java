package com.triplify.application.usecase.story.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.tag.dto.TagResponse;

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
        Set<TagResponse> tags,
        Set<ImageResponse> images
) {
}
