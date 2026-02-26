package com.triplify.domain.model.media;

import java.time.LocalDateTime;
import java.util.UUID;

public record Image(
        UUID id,
        String url,
        String storageKey,
        String description,
        UUID tripId,
        UUID tripPlaceId,
        UUID tripRouteId,
        UUID storyId,
        LocalDateTime uploadedAt
) {
}
