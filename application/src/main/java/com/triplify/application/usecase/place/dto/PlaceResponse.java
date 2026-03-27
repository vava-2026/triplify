package com.triplify.application.usecase.place.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;

import java.time.Instant;

public record PlaceResponse(
        String id,
        String userId,
        String countryId,
        ImageResponse coverImage,
        String title,
        String description,
        double latitude,
        double longitude,
        Instant createdAt,
        Instant updatedAt
) {
}
