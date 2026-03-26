package com.triplify.application.usecase.triproute.dto;

import com.triplify.domain.model.enums.StatusEnum;

import java.time.Instant;
import java.util.Set;

public record TripRouteResponse(
        String id,
        String tripId,
        String routeId,
        int order,
        StatusEnum status,
        Instant startedAt,
        Instant endedAt,
        Instant createdAt,
        Instant updatedAt,
        Set<String> imageIds
) {
}
