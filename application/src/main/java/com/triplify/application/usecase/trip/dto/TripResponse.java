package com.triplify.application.usecase.trip.dto;

import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.domain.model.enums.StatusEnum;

import java.time.Instant;
import java.util.Set;

public record TripResponse(
        String id,
        String userId,
        CategoryResponse category,
        String title,
        String description,
        StatusEnum status,
        Instant startedAt,
        Instant endedAt,
        Instant createdAt,
        Instant updatedAt,
        Set<TagResponse> tags,
        Set<ImageResponse> images,
        Set<CountryResponse> countries
) {
}
