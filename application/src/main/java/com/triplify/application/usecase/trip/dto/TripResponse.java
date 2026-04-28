package com.triplify.application.usecase.trip.dto;

import com.triplify.application.shared.ColorTheme;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.domain.model.Category;
import com.triplify.domain.model.Trip;
import com.triplify.domain.model.enums.StatusEnum;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record TripResponse(
        UUID id,
        UUID userId,
        CategoryResponse category,
        String title,
        String description,
        StatusEnum status,
        Instant startedAt,
        Instant endedAt,
        Instant createdAt,
        Instant updatedAt,
        Set<TagResponse> tags,
        ImageResponse coverImage,
        Set<CountryResponse> countries
) {

    public static TripResponse from(Trip trip) {
        Set<TagResponse> tagResponses = trip.getTags().stream()
                .map(TagResponse::from)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<CountryResponse> countryResponses = trip.getCountries().stream()
                .map(CountryResponse::from)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new TripResponse(
                trip.getId(),
                trip.getUserId(),
                trip.getCategory() == null ? null : CategoryResponse.from(trip.getCategory()),
                trip.getTitle(),
                trip.getDescription(),
                trip.getStatus(),
                trip.getStartedAt(),
                trip.getEndedAt(),
                trip.getCreatedAt(),
                trip.getUpdatedAt(),
                tagResponses,
                trip.getCoverImage() == null ? null : ImageResponse.from(trip.getCoverImage()),
                countryResponses
        );
    }
}
