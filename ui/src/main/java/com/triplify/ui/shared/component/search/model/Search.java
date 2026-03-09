package com.triplify.ui.shared.component.search.model;

import com.triplify.ui.shared.component.entry.model.Entry;

import lombok.Getter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Search<T> {

    private final Function<String, List<Entry<T>>> searchFunction;

    @Getter private final String placeholder;
    @Getter private final int debounceMs;
    @Getter private final int maxResults;
    @Getter private final boolean caseSensitive;
    @Getter private final Consumer<Entry<T>> onResultSelected;
    @Getter private final String noResultsMessage;
    @Getter private final SearchVariant variant;

    private Search(Builder<T> builder) {
        this.searchFunction = builder.searchFunction;
        this.placeholder = builder.placeholder;
        this.debounceMs = builder.debounceMs;
        this.maxResults = builder.maxResults;
        this.caseSensitive = builder.caseSensitive;
        this.onResultSelected = builder.onResultSelected;
        this.noResultsMessage = builder.noResultsMessage;
        this.variant = builder.variant;
    }

    public List<Entry<T>> search(String query) {
        if (query == null) {
            return List.of();
        }
        String effectiveQuery = caseSensitive ? query : query.toLowerCase();
        List<Entry<T>> results = searchFunction.apply(effectiveQuery);
        return maxResults > 0 && results.size() > maxResults
                ? results.subList(0, maxResults)
                : results;
    }

    public void selectResult(Entry<T> result) {
        if (onResultSelected != null) {
            onResultSelected.accept(result);
        }
    }

    public static <T> Builder<T> builder(Function<String, List<Entry<T>>> searchFunction) {
        return new Builder<>(searchFunction);
    }

    public static class Builder<T> {

        static private final String DEFAULT_PLACEHOLDER = "Search...";
        static private final String DEFAULT_NO_RESULT_MESSAGE = "No results found.";
        static private final int DEFAULT_DEBOUNCE_MS = 300;
        static private final int DEFAULT_MAX_RESULTS = 0;   // unlimited
        static private final SearchVariant DEFAULT_VARIANT = SearchVariant.WHITE;

        private final Function<String, List<Entry<T>>> searchFunction;
        private Consumer<Entry<T>> onResultSelected;
        private String placeholder = DEFAULT_PLACEHOLDER;
        private String noResultsMessage = DEFAULT_NO_RESULT_MESSAGE;
        private int debounceMs = DEFAULT_DEBOUNCE_MS;
        private int maxResults = DEFAULT_MAX_RESULTS;
        private SearchVariant variant = DEFAULT_VARIANT;
        private boolean caseSensitive = false;

        private Builder(Function<String, List<Entry<T>>> searchFunction) {
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

        public Builder<T> onResultSelected(Consumer<Entry<T>> onResultSelected) {
            this.onResultSelected = onResultSelected;
            return this;
        }

        public Builder<T> noResultsMessage(String noResultsMessage) {
            this.noResultsMessage = noResultsMessage;
            return this;
        }

        public Builder<T> variant(SearchVariant variant) {
            this.variant = variant;
            return this;
        }

        public Search<T> build() {
            return new Search<>(this);
        }
    }
}
