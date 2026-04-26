package com.triplify.ui.shared.component.routes.model;

import com.triplify.application.usecase.route.RouteService;
import com.triplify.application.usecase.route.dto.GetRoutesRequest;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.domain.error.AppError;
import com.triplify.domain.pagination.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Routes {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final RouteService routeService;
    private final Consumer<AppError> onLoadFailed;
    private final int pageSize;

    private final List<RouteResponse> entries = new ArrayList<>();
    private String activeQuery = "";
    private int nextPage = 0;
    private boolean hasMore = true;
    private boolean loading = false;

    private Routes(Builder builder) {
        this.routeService = builder.routeService;
        this.onLoadFailed = builder.onLoadFailed;
        this.pageSize = builder.pageSize;
    }

    public List<RouteResponse> search(String query) {
        String normalized = normalize(query);
        if (!normalized.equals(activeQuery)) {
            resetState(normalized);
        }
        if (entries.isEmpty() && nextPage == 0) {
            loadNextPage();
        }
        return List.copyOf(entries);
    }

    public List<RouteResponse> loadMore(String query) {
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
            GetRoutesRequest.Filter filter = nameFilter == null ? null : new GetRoutesRequest.Filter(nameFilter);
            var request = new GetRoutesRequest(new PageRequest(nextPage, pageSize), filter);
            var result = routeService.getRoutes(request);
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

    public static Builder builder(RouteService routeService) {
        return new Builder(routeService);
    }

    public static class Builder {
        private final RouteService routeService;
        private Consumer<AppError> onLoadFailed;
        private int pageSize = DEFAULT_PAGE_SIZE;

        private Builder(RouteService routeService) {
            this.routeService = routeService;
        }

        public Builder onLoadFailed(Consumer<AppError> onLoadFailed) {
            this.onLoadFailed = onLoadFailed;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Routes build() {
            return new Routes(this);
        }
    }
}
