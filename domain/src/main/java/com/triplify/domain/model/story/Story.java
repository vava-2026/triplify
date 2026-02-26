package com.triplify.domain.model.story;

import java.time.LocalDateTime;
import java.util.UUID;

public record Story(
        UUID id,
        UUID userId,
        UUID tripId,
        UUID placeId,
        UUID routeId,
        UUID emotionId,
        String title,
        String description,
        LocalDateTime time,
        double latitude,
        double longitude,
        LocalDateTime createdAt
) {
}
