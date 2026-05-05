package com.triplify.application.usecase.triproute.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.domain.model.enums.StatusEnum;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record TripRouteResponse(
        UUID id,
        UUID tripId,
        RouteResponse route,
        int order,
        StatusEnum status,
        Instant startedAt,
        Instant endedAt,
        Instant createdAt,
        Instant updatedAt,
        Set<ImageResponse> images
) {
}
