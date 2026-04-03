package com.triplify.application.usecase.place.dto;

import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.domain.model.Place;

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
    public static PlaceResponse from(Place place, CountryResponse country, ImageResponse image) {
        return new PlaceResponse(
                place.getId().toString(),
                place.getUserId().toString(),
                country,
                image,
                place.getTitle(),
                place.getDescription(),
                place.getLatitude(),
                place.getLongitude(),
                place.getCreatedAt(),
                place.getUpdatedAt()
        );
    }
}
