package com.triplify.domain.model.route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RoutePlace(
        UUID id,
        UUID userId,
        UUID routeId,
        UUID placeId,
        int priority,
        LocalDateTime createdAt,
        String status,
        LocalDate startDate,
        LocalDate endDate
) {
}
