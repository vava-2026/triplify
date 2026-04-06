package com.triplify.domain.filter;

import java.time.Instant;
import java.util.UUID;

public record StoryFilter(
        UUID userId,
        String tripId,
        String tripRouteId,
        String tripPlaceId,
        String title,
        Instant storyTimeFrom,
        Instant storyTimeTo,
        boolean storyTimeAsc
) {
    public StoryFilter {
        if (userId == null) throw new IllegalArgumentException("userId must not be null");
        tripId      = tripId      == null ? null : tripId.trim();
        tripRouteId = tripRouteId == null ? null : tripRouteId.trim();
        tripPlaceId = tripPlaceId == null ? null : tripPlaceId.trim();
        title       = title       == null ? null : title.trim();
    }
}
