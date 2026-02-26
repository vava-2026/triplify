package com.triplify.domain.model.trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Trip(
        UUID id,
        UUID userId,
        UUID categoryId,
        UUID countryId,
        String title,
        String description,
        UUID imageId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        LocalDateTime createdAt
) {
}
