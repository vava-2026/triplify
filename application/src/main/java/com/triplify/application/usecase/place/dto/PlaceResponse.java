package com.triplify.application.usecase.place.dto;

import java.time.Instant;

public record PlaceResponse(
        String id,
        String userId,
        String countryId,
        String coverImageId,
        String title,
        String description,
        double latitude,
        double longitude,
        Instant createdAt,
        Instant updatedAt
) {
}
