package com.triplify.ui.storage;

import com.triplify.application.usecase.trip.dto.TripStatus;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.domain.model.enums.TripPlaceSourceType;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EditorDraftStorage {

    public static final String TARGET_TRIP = "trip";
    public static final String TARGET_ROUTE = "route";

    private static TripDraft tripDraft;
    private static RouteDraft routeDraft;
    private static PendingPlace pendingPlace;
    private static PendingRoute pendingRoute;

    private EditorDraftStorage() {
    }

    public static synchronized void saveTripDraft(TripDraft draft) {
        tripDraft = draft;
    }

    public static synchronized TripDraft consumeTripDraft() {
        TripDraft draft = tripDraft;
        tripDraft = null;
        return draft;
    }

    public static synchronized void clearTripDraft() {
        tripDraft = null;
    }

    public static synchronized void saveRouteDraft(RouteDraft draft) {
        routeDraft = draft;
    }

    public static synchronized RouteDraft consumeRouteDraft() {
        RouteDraft draft = routeDraft;
        routeDraft = null;
        return draft;
    }

    public static synchronized void clearRouteDraft() {
        routeDraft = null;
    }

    public static synchronized void savePendingPlace(String target, PlaceResponse place) {
        if (target == null || target.isBlank() || place == null) {
            return;
        }
        pendingPlace = new PendingPlace(target, place);
    }

    public static synchronized PlaceResponse consumePendingPlace(String target) {
        if (pendingPlace == null || target == null || !target.equals(pendingPlace.target())) {
            return null;
        }
        PlaceResponse place = pendingPlace.place();
        pendingPlace = null;
        return place;
    }

    public static synchronized void savePendingRoute(String target, RouteResponse route) {
        if (target == null || target.isBlank() || route == null) {
            return;
        }
        pendingRoute = new PendingRoute(target, route);
    }

    public static synchronized RouteResponse consumePendingRoute(String target) {
        if (pendingRoute == null || target == null || !target.equals(pendingRoute.target())) {
            return null;
        }
        RouteResponse route = pendingRoute.route();
        pendingRoute = null;
        return route;
    }

    public record TripDraft(
            String tripId,
            String title,
            String description,
            String currentTripDisplayName,
            TripStatus tripStatus,
            LocalDate startDate,
            LocalDate endDate,
            String categoryValue,
            Set<String> selectedCountryIds,
            Map<String, String> selectedCountryLabelsById,
            Set<String> selectedTagLabels,
            Set<String> selectedTagIds,
            String coverImagePath,
            boolean coverImageDirty,
            List<RouteDraftItem> routes,
            List<PlaceDraftItem> manualPlaces
    ) {

        public TripDraft {
            selectedCountryIds = selectedCountryIds == null ? Set.of() : new LinkedHashSet<>(selectedCountryIds);
            selectedCountryLabelsById = selectedCountryLabelsById == null
                    ? Map.of()
                    : new LinkedHashMap<>(selectedCountryLabelsById);
            selectedTagLabels = selectedTagLabels == null ? Set.of() : new LinkedHashSet<>(selectedTagLabels);
            selectedTagIds = selectedTagIds == null ? Set.of() : new LinkedHashSet<>(selectedTagIds);
            routes = routes == null ? List.of() : List.copyOf(routes);
            manualPlaces = manualPlaces == null ? List.of() : List.copyOf(manualPlaces);
        }
    }

    public record RouteDraft(
            String routeId,
            String tripId,
            String tripName,
            String title,
            String description,
            String coverImagePath,
            List<PlaceDraftItem> places
    ) {

        public RouteDraft {
            places = places == null ? List.of() : List.copyOf(places);
        }
    }

    public record RouteDraftItem(
            String id,
            String title,
            String subtitle,
            String imagePath,
            List<PlaceDraftItem> derivedPlaces
    ) {

        public RouteDraftItem {
            derivedPlaces = derivedPlaces == null ? List.of() : List.copyOf(derivedPlaces);
        }
    }

    public record PlaceDraftItem(
            String id,
            String title,
            String subtitle,
            String imagePath,
            TripPlaceSourceType sourceType,
            String sourceRouteId,
            Double latitude,
            Double longitude
    ) {
    }

    private record PendingPlace(String target, PlaceResponse place) {
    }

    private record PendingRoute(String target, RouteResponse route) {
    }
}
