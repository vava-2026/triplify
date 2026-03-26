package com.triplify.application.usecase.route.dto;

import java.time.Instant;
import java.util.Set;
import java.util.SortedSet;

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
        SortedSet<String> placeIds
) {
}
