package com.triplify.domain.model.route;

import java.time.LocalDateTime;
import java.util.UUID;

public record Route(
        UUID id,
        UUID userId,
        UUID imageId,
        String title,
        String description,
        double totalLength,
        String status,
        LocalDateTime createdAt
) {
}
