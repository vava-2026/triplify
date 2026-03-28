package com.triplify.application.usecase.triproute.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.domain.model.enums.StatusEnum;

import java.time.Instant;
import java.util.Set;

public record TripRouteResponse(
        String id,
        String tripId,
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
