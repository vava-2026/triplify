package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;

import java.time.Instant;
import java.util.Set;

public record TripPlaceResponse(
        String id,
        String tripId,
        PlaceResponse place,
        Instant visitDate,
        Instant createdAt,
        Instant updatedAt,
        Set<ImageResponse> images
) {
}
