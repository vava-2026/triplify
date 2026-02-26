package com.triplify.domain.model.trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TripPlace(
        UUID id,
        UUID userId,
        UUID placeId,
        String type,
        UUID tripId,
        UUID routeId,
        int priority,
        LocalDateTime createdAt,
        String status,
        LocalDate visitDate
) {
}
