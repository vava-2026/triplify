package com.triplify.ui.shared.component.search.model;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Search<T> {

    private final Function<String, List<T>> searchFunction;
    private final String placeholder;
    private final int debounceMs;
    private final int maxResults;
    private final boolean caseSensitive;
    private final Consumer<T> onResultSelected;
    private final String noResultsMessage;

    private Search(Builder<T> builder) {
        this.searchFunction = builder.searchFunction;
        this.placeholder = builder.placeholder;
        this.debounceMs = builder.debounceMs;
        this.maxResults = builder.maxResults;
        this.caseSensitive = builder.caseSensitive;
        this.onResultSelected = builder.onResultSelected;
        this.noResultsMessage = builder.noResultsMessage;
    }

    public List<T> search(String query) {
        if (query == null) {
            return List.of();
        }
        String effectiveQuery = caseSensitive ? query : query.toLowerCase();
        List<T> results = searchFunction.apply(effectiveQuery);
        return maxResults > 0 && results.size() > maxResults
                ? results.subList(0, maxResults)
                : results;
    }

    public void selectResult(T result) {
        if (onResultSelected != null) {
            onResultSelected.accept(result);
        }
    }

    // Getters
    public String getPlaceholder() { return placeholder; }
    public int getDebounceMs() { return debounceMs; }
    public int getMaxResults() { return maxResults; }
    public boolean isCaseSensitive() { return caseSensitive; }
    public String getNoResultsMessage() { return noResultsMessage; }

    public static <T> Builder<T> builder(Function<String, List<T>> searchFunction) {
        return new Builder<>(searchFunction);
    }

    public static class Builder<T> {

        private Function<String, List<T>> searchFunction;
        private String placeholder = "Search...";
        private int debounceMs = 300;
        private int maxResults = 0; // 0 = unlimited
        private boolean caseSensitive = false;
        private Consumer<T> onResultSelected;
        private String noResultsMessage = "No results found.";

        private Builder(Function<String, List<T>> searchFunction) {
            if (searchFunction == null) {
                throw new IllegalArgumentException("searchFunction must not be null");
            }
            this.searchFunction = searchFunction;
        }

        public Builder<T> placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder<T> debounceMs(int debounceMs) {
            this.debounceMs = debounceMs;
            return this;
        }

        public Builder<T> maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder<T> caseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        public Builder<T> onResultSelected(Consumer<T> onResultSelected) {
            this.onResultSelected = onResultSelected;
            return this;
        }

        public Builder<T> noResultsMessage(String noResultsMessage) {
            this.noResultsMessage = noResultsMessage;
            return this;
        }

        public Search<T> build() {
            return new Search<>(this);
        }
    }
}
