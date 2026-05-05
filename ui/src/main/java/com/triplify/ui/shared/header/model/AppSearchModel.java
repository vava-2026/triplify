package com.triplify.ui.shared.header.model;

import com.triplify.application.shared.ColorTheme;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.GetRoutesRequest;
import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.GetTripsRequest;
import com.triplify.domain.error.AppError;
import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.pagination.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AppSearchModel {

    private final TripService tripService;
    private final RouteService routeService;
    private final PlaceService placeService;
    private final Consumer<AppError> onLoadFailed;
    
    public AppSearchModel(TripService tripService, RouteService routeService, PlaceService placeService, Consumer<AppError> onLoadFailed) {
        this.tripService = tripService;
        this.routeService = routeService;
        this.placeService = placeService;
        this.onLoadFailed = onLoadFailed;
    }
    
    public List<GlobalSearchItem> search(String query) {
        List<GlobalSearchItem> results = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return results;
        }
        
        // trips
        try {
            var filter = new GetTripsRequest.Filter(query, null, null, null, null, null, null);
            var result = tripService.getTrips(new GetTripsRequest(new PageRequest(0, 5), filter, null));
            result.onSuccess(page -> page.items().forEach(t -> {
                ColorTheme theme = ColorTheme.GRAY;
                if (t.status() != null) {
                    theme = switch (t.status()) {
                        case PLANNED -> ColorTheme.BLUE;
                        case ONGOING -> ColorTheme.GREEN;
                        case VISITED -> ColorTheme.TEAL;
                        case CANCELED -> ColorTheme.GRAY;
                    };
                }
                results.add(GlobalSearchItem.builder()
                    .id(t.id())
                    .title(t.title() != null && !t.title().isBlank() ? t.title() + " (Trip)" : "—")
                    .type(GlobalSearchItem.Type.TRIP)
                    .colorTheme(theme)
                    .build());
            }));
            result.onFailure(e -> { if (onLoadFailed != null) onLoadFailed.accept(e); });
        } catch (Exception ignored) {}
        
        // routes
        try {
            var filter = new GetRoutesRequest.Filter(query);
            var result = routeService.getRoutes(new GetRoutesRequest(new PageRequest(0, 5), filter, null));
            result.onSuccess(page -> page.items().forEach(r -> {
                results.add(GlobalSearchItem.builder()
                    .id(r.id())
                    .title((r.title() != null ? r.title() : "") + " (Route)")
                    .type(GlobalSearchItem.Type.ROUTE)
                    .colorTheme(ColorTheme.VIOLET)
                    .build());
            }));
        } catch (Exception ignored) {}
        
        // places
        try {
            var filter = new PlaceFilter(query, null);
            var result = placeService.getPlaces(new GetPlacesRequest(new PageRequest(0, 5), filter));
            result.onSuccess(page -> page.items().forEach(p -> {
                results.add(GlobalSearchItem.builder()
                    .id(p.id())
                    .title((p.title() != null ? p.title() : "") + " (Place)")
                    .type(GlobalSearchItem.Type.PLACE)
                    .colorTheme(ColorTheme.ORANGE)
                    .build());
            }));
        } catch (Exception ignored) {}

        return results;
    }
}
