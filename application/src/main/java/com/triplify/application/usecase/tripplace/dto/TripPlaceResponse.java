package com.triplify.application.usecase.tripplace.dto;

import java.time.Instant;
import java.util.Set;

public record TripPlaceResponse(
        String id,
        String tripId,
        String placeId,
        Instant visitDate,
        Instant createdAt,
        Instant updatedAt,
        Set<String> imageIds
) {
}
