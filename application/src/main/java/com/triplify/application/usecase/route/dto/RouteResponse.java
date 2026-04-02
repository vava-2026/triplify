package com.triplify.application.usecase.route.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;

import java.time.Instant;
import java.util.Set;
import java.util.SortedSet;

public record RouteResponse(
        String id,
        String userId,
        ImageResponse coverImage,
        String title,
        String description,
        double length,
        Instant createdAt,
        Instant updatedAt,
        Set<ImageResponse> images,
        SortedSet<PlaceResponse> places
) {
}
