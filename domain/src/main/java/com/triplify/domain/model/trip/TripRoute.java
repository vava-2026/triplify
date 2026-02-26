package com.triplify.domain.model.trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TripRoute(
        UUID id,
        UUID userId,
        UUID tripId,
        UUID routeId,
        int priority,
        LocalDateTime createdAt,
        String status,
        LocalDate startDate,
        LocalDate endDate
) {
}
