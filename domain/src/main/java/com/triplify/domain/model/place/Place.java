package com.triplify.domain.model.place;

import java.time.LocalDateTime;
import java.util.UUID;

public record Place(
        UUID id,
        UUID userId,
        String title,
        String description,
        UUID countryId,
        UUID imageId,
        double latitude,
        double longitude,
        LocalDateTime createdAt
) {
}
