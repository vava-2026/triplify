package com.triplify.ui.shared.component.places.model;

import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.domain.error.AppError;
import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.pagination.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Places {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PlaceService placeService;
    private final Consumer<AppError> onLoadFailed;
    private final int pageSize;

    private final List<PlaceResponse> entries = new ArrayList<>();
    private String activeQuery = "";
    private int nextPage = 0;
    private boolean hasMore = true;
    private boolean loading = false;

    private Places(Builder builder) {
        this.placeService = builder.placeService;
        this.onLoadFailed = builder.onLoadFailed;
        this.pageSize = builder.pageSize;
    }

    public List<PlaceResponse> search(String query) {
        String normalized = normalize(query);
        if (!normalized.equals(activeQuery)) {
            resetState(normalized);
        }
        if (entries.isEmpty() && nextPage == 0) {
            loadNextPage();
        }
        return List.copyOf(entries);
    }

    public List<PlaceResponse> loadMore(String query) {
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
        if (loading || !hasMore) {
            return;
        }
        loading = true;
        try {
            String nameFilter = activeQuery.isBlank() ? null : activeQuery;
            var request = new GetPlacesRequest(new PageRequest(nextPage, pageSize), new PlaceFilter(nameFilter, null));
            var result = placeService.getPlaces(request);
            result.onSuccess(page -> {
                entries.addAll(page.items());
                hasMore = page.hasNext();
                nextPage++;
            });
            result.onFailure(error -> {
                hasMore = false;
                if (onLoadFailed != null) {
                    onLoadFailed.accept(error);
                }
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

    public static Builder builder(PlaceService placeService) {
        return new Builder(placeService);
    }

    public static class Builder {
        private final PlaceService placeService;
        private Consumer<AppError> onLoadFailed;
        private int pageSize = DEFAULT_PAGE_SIZE;

        private Builder(PlaceService placeService) {
            this.placeService = placeService;
        }

        public Builder onLoadFailed(Consumer<AppError> onLoadFailed) {
            this.onLoadFailed = onLoadFailed;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Places build() {
            return new Places(this);
        }
    }
}
