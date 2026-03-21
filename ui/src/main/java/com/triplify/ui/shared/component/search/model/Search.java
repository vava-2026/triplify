package com.triplify.ui.shared.component.search.model;

import com.triplify.ui.shared.component.select.entry.model.Entry;

import com.triplify.ui.shared.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Search<T> {

    private final Function<String, List<Entry<T>>> searchFunction;

    @Getter private final StringProperty placeholder = new SimpleStringProperty();
    @Getter private final StringProperty noResult = new SimpleStringProperty();
    @Getter private final int debounceMs;
    @Getter private final int maxResults;
    @Getter private final boolean caseSensitive;
    @Getter private final Consumer<Entry<T>> onResultSelected;
    @Getter private final SearchVariant variant;

    private Search(Builder<T> builder) {
        this.searchFunction = builder.searchFunction;
        this.debounceMs = builder.debounceMs;
        this.maxResults = builder.maxResults;
        this.caseSensitive = builder.caseSensitive;
        this.onResultSelected = builder.onResultSelected;
        this.variant = builder.variant;

        Localization.bindText(placeholder, builder.placeholderKey);
        Localization.bindText(noResult, builder.noResultKey);
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

        static private final String DEFAULT_PLACEHOLDER_KEY = "search.placeholder";
        static private final String DEFAULT_NO_RESULT_KEY = "search.noResult";
        static private final int DEFAULT_DEBOUNCE_MS = 300;
        static private final int DEFAULT_MAX_RESULTS = 0;   // unlimited
        static private final SearchVariant DEFAULT_VARIANT = SearchVariant.WHITE;

        private final Function<String, List<Entry<T>>> searchFunction;
        private Consumer<Entry<T>> onResultSelected;
        private String placeholderKey = DEFAULT_PLACEHOLDER_KEY;
        private String noResultKey = DEFAULT_NO_RESULT_KEY;
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

        public Builder<T> placeholderKey(String placeholderKey) {
            this.placeholderKey = placeholderKey;
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

        public Builder<T> noResultKey(String noResultKey) {
            this.noResultKey = noResultKey;
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
