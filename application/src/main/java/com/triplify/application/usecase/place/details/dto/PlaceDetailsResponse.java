package com.triplify.application.usecase.place.details.dto;

import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.story.dto.StoryResponse;

import java.util.List;

public record PlaceDetailsResponse(
        PlaceResponse place,
        List<PlaceResponse> associatedPlaces,
        List<RouteResponse> associatedRoutes,
        List<StoryResponse> associatedStories
) {
    public PlaceDetailsResponse {
        associatedPlaces = associatedPlaces == null ? List.of() : List.copyOf(associatedPlaces);
        associatedRoutes = associatedRoutes == null ? List.of() : List.copyOf(associatedRoutes);
        associatedStories = associatedStories == null ? List.of() : List.copyOf(associatedStories);
    }
}
