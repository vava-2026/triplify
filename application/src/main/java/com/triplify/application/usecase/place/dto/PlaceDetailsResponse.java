package com.triplify.application.usecase.place.dto;

import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.trip.dto.TripResponse;

import java.util.List;

public record PlaceDetailsResponse(
        PlaceResponse place,
        List<TripResponse> associatedTrips,
        List<RouteResponse> associatedRoutes,
        List<StoryResponse> associatedStories
) {
    public PlaceDetailsResponse {
        associatedTrips = associatedTrips == null ? List.of() : List.copyOf(associatedTrips);
        associatedRoutes = associatedRoutes == null ? List.of() : List.copyOf(associatedRoutes);
        associatedStories = associatedStories == null ? List.of() : List.copyOf(associatedStories);
    }
}
