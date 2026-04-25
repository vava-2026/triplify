package com.triplify.ui.shared.component.trips.model;

import com.triplify.application.usecase.trip.TripService;
import com.triplify.application.usecase.trip.dto.GetTripsRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.domain.error.AppError;
import com.triplify.domain.pagination.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Trips {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final TripService tripService;
    private final Consumer<AppError> onLoadFailed;
    private final int pageSize;

    private final List<TripResponse> entries = new ArrayList<>();
    private String activeQuery = "";
    private int nextPage = 0;
    private boolean hasMore = true;
    private boolean loading = false;

    private Trips(Builder builder) {
        this.tripService = builder.tripService;
        this.onLoadFailed = builder.onLoadFailed;
        this.pageSize = builder.pageSize;
    }

    public List<TripResponse> search(String query) {
        String normalized = normalize(query);
        if (!normalized.equals(activeQuery)) {
            resetState(normalized);
        }
        if (entries.isEmpty() && nextPage == 0) {
            loadNextPage();
        }
        return List.copyOf(entries);
    }

    public List<TripResponse> loadMore(String query) {
        String normalized = normalize(query);
        if (!normalized.equals(activeQuery)) {
            return search(normalized);
        }
        int start = entries.size();
        loadNextPage();
        if (entries.size() <= start) {
            return List.of();
        }
        return List.copyOf(entries.subList(start, entries.size()));
    }

    public void reset() {
        resetState(activeQuery);
    }

    private void loadNextPage() {
        if (loading || !hasMore) return;
        loading = true;
        try {
            String nameFilter = activeQuery.isBlank() ? null : activeQuery;
            GetTripsRequest.Filter filter = nameFilter == null
                    ? null
                    : new GetTripsRequest.Filter(nameFilter, null, null, null, null, null, null);
            var request = new GetTripsRequest(new PageRequest(nextPage, pageSize), filter, null);
            var result = tripService.getTrips(request);
            result.onSuccess(page -> {
                entries.addAll(page.items());
                hasMore = page.hasNext();
                nextPage++;
            });
            result.onFailure(error -> {
                hasMore = false;
                if (onLoadFailed != null) onLoadFailed.accept(error);
            });
        } finally {
            loading = false;
        }
    }

    private void resetState(String query) {
        activeQuery = query;
        nextPage = 0;
        hasMore = true;
        entries.clear();
    }

    private String normalize(String query) {
        return query == null ? "" : query.trim();
    }

    public static Builder builder(TripService tripService) {
        return new Builder(tripService);
    }

    public static class Builder {
        private final TripService tripService;
        private Consumer<AppError> onLoadFailed;
        private int pageSize = DEFAULT_PAGE_SIZE;

        private Builder(TripService tripService) {
            this.tripService = tripService;
        }

        public Builder onLoadFailed(Consumer<AppError> onLoadFailed) {
            this.onLoadFailed = onLoadFailed;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Trips build() {
            return new Trips(this);
        }
    }
}
