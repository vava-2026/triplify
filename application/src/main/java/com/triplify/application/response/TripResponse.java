package com.triplify.application.response;

import java.time.LocalDate;
import java.util.List;

public record TripResponse(
        String id,
        String name,
        String country,
        String category,
        TripStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String coverKey,
        String coverUrl,
        List<String> tags
) {
}
