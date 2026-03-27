package com.triplify.application.usecase.trip.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.domain.model.enums.StatusEnum;

import java.time.Instant;
import java.util.Set;

public record TripResponse(
        String id,
        String userId,
        String categoryId,
        String title,
        String description,
        StatusEnum status,
        Instant startedAt,
        Instant endedAt,
        Instant createdAt,
        Instant updatedAt,
        Set<String> tagIds,
        Set<ImageResponse> images,
        Set<String> countryIds
) {
}
