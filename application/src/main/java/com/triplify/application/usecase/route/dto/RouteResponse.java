package com.triplify.application.usecase.route.dto;

import com.triplify.domain.model.enums.StatusEnum;

import java.time.Instant;
import java.util.Set;

public record RouteResponse(
        String id,
        String userId,
        String coverImageId,
        String title,
        String description,
        double length,
        Instant createdAt,
        Instant updatedAt,
        Set<String> imageIds,
        Set<String> placeIds
) {
}
