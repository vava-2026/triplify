package com.triplify.application.usecase.route.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.domain.model.Route;
import com.triplify.domain.model.RoutePlace;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public record RouteResponse(
        UUID id,
        UUID userId,
        ImageResponse coverImage,
        String title,
        String description,
        double length,
        Instant createdAt,
        Instant updatedAt,
        Set<ImageResponse> images,
        SortedSet<PlaceResponse> places
) {
    public static RouteResponse from(Route route, Collection<RoutePlace> routePlaces) {
        Map<String, Integer> orderByPlaceId = new HashMap<>();
        for (RoutePlace routePlace : routePlaces) {
            orderByPlaceId.put(routePlace.getPlaceId().toString(), routePlace.getOrder());
        }

        SortedSet<PlaceResponse> places = new TreeSet<>(
                Comparator.<PlaceResponse>comparingInt(place -> orderByPlaceId.getOrDefault(place.id(), Integer.MAX_VALUE))
                        .thenComparing(PlaceResponse::title, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(PlaceResponse::id)
        );

        for (RoutePlace routePlace : routePlaces) {
            if (routePlace.getPlace() != null) {
                places.add(PlaceResponse.from(routePlace.getPlace()));
            }
        }

        return new RouteResponse(
                route.getId(),
                route.getUserID(),
                route.getCoverImage() != null ? ImageResponse.from(route.getCoverImage()) : null,
                route.getTitle(),
                route.getDescription(),
                route.getLength() != null ? route.getLength() : 0D,
                route.getCreatedAt(),
                route.getUpdatedAt(),
                Set.of(),
                places
        );
    }
}
