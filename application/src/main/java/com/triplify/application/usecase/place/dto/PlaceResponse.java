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
    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId().toString(),
                place.getUserId().toString(),
                place.getCountry() != null ? CountryResponse.from(place.getCountry()) : null,
                place.getCoverImage() != null ? ImageResponse.from(place.getCoverImage()) : null,
                place.getTitle(),
                place.getDescription(),
                place.getLatitude(),
                place.getLongitude(),
                place.getCreatedAt(),
                place.getUpdatedAt()
        );
    }
}
