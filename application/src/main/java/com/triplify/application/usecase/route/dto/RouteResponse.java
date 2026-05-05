package com.triplify.application.usecase.route.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.domain.model.Route;
import com.triplify.domain.model.RoutePlace;

import java.time.Instant;
import java.util.*;

public record RouteResponse(
        UUID id,
        UUID userId,
        ImageResponse coverImage,
        String title,
        String description,
        double length,
        Instant createdAt,
        Instant updatedAt,
        List<PlaceResponse> places
) {
    public static RouteResponse from(Route route, Collection<RoutePlace> routePlaces) {
        var sortedPlaces = routePlaces.stream().sorted(Comparator.comparingInt(RoutePlace::getOrder));

        return new RouteResponse(
                route.getId(),
                route.getUserID(),
                route.getCoverImage() != null ? ImageResponse.from(route.getCoverImage()) : null,
                route.getTitle(),
                route.getDescription(),
                route.getLength() != null ? route.getLength() : 0D,
                route.getCreatedAt(),
                route.getUpdatedAt(),
                sortedPlaces.map(rp -> PlaceResponse.from(rp.getPlace())).toList()
        );
    }
}
