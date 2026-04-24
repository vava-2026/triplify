package com.triplify.domain.filter;

import java.time.Instant;
import java.util.UUID;

public record StoryFilter(
        UUID userId,
        UUID tripId,
        UUID tripRouteId,
        UUID tripPlaceId,
        String title,
        Instant storyTimeFrom,
        Instant storyTimeTo,
        boolean storyTimeAsc
) {
    public StoryFilter {
        title = title != null ? title.trim() : null;
    }
}
