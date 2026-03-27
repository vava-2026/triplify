package com.triplify.application.usecase.place.dto;

import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;

import java.time.Instant;

public record PlaceResponse(
        String id,
        String userId,
        CountryResponse country,
        ImageResponse coverImage,
        String title,
        String description,
        double latitude,
        double longitude,
        Instant createdAt,
        Instant updatedAt
) {
}
