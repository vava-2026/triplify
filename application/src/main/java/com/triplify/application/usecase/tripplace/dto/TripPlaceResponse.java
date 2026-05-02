package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.model.enums.TripPlaceSourceType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record TripPlaceResponse(
        UUID id,
        UUID tripId,
        PlaceResponse place,
        TripPlaceSourceType sourceType,
        UUID tripRouteId,
        UUID routePlaceId,
        Instant visitDate,
        StatusEnum status,
        Instant createdAt,
        Instant updatedAt,
        Set<ImageResponse> images
) {
}
