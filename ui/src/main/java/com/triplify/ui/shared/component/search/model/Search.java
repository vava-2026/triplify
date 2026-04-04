package com.triplify.ui.shared.component.search.model;

import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.search.model.SearchDisplayMode;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Search<T> {

    private final Function<String, List<Entry<T>>> searchFunction;
    private final Function<String, List<Entry<T>>> loadMoreFunction;

    @Getter private final StringProperty placeholder = new SimpleStringProperty();
    @Getter private final StringProperty noResult = new SimpleStringProperty();
    @Getter private final int debounceMs;
    @Getter private final int maxResults;
    @Getter private final int maxVisibleResults;
    @Getter private final boolean searchOnTyping;
    @Getter private final boolean showOnEmptyQuery;
    @Getter private final double loadMoreThreshold;
    @Getter private final SearchSize size;
    @Getter private final SearchDisplayMode displayMode;
    @Getter private final Consumer<Entry<T>> onResultSelected;
    @Getter private final FieldVariant variant;

    private Search(Builder<T> builder) {
        this.searchFunction = builder.searchFunction;
        this.loadMoreFunction = builder.loadMoreFunction;
        this.debounceMs = builder.debounceMs;
        this.maxResults = builder.maxResults;
        this.maxVisibleResults = builder.maxVisibleResults;
        this.searchOnTyping = builder.searchOnTyping;
        this.showOnEmptyQuery = builder.showOnEmptyQuery;
        this.loadMoreThreshold = builder.loadMoreThreshold;
        this.size = builder.size;
        this.displayMode = builder.displayMode;
        this.onResultSelected = builder.onResultSelected;
        this.variant = builder.variant;

        Localization.bindText(placeholder, builder.placeholderKey);
        Localization.bindText(noResult, builder.noResultKey);
    }

    public List<Entry<T>> search(String query) {
        return getEntries(query, query == null, searchFunction);
    }

    private List<Entry<T>> getEntries(String query, boolean b, Function<String, List<Entry<T>>> searchFunction) {
        if (b) {
            return List.of();
        }
        List<Entry<T>> results = searchFunction.apply(query);
        return maxResults > 0 && results.size() > maxResults
                ? results.subList(0, maxResults)
                : results;
    }

    public void selectResult(Entry<T> result) {
        if (onResultSelected != null) {
            onResultSelected.accept(result);
        }
    }

    public boolean canLoadMore() {
        return loadMoreFunction != null;
    }

    public List<Entry<T>> loadMore(String query) {
        return getEntries(query, loadMoreFunction == null, loadMoreFunction);
    }

    public static <T> Builder<T> builder(Function<String, List<Entry<T>>> searchFunction) {
        return new Builder<>(searchFunction);
    }

    public static class Builder<T> {

        static private final String DEFAULT_PLACEHOLDER_KEY = "search.placeholder";
        static private final String DEFAULT_NO_RESULT_KEY = "search.noResult";
        static private final int DEFAULT_DEBOUNCE_MS = 300;
        static private final int DEFAULT_MAX_RESULTS = 0;
        static private final int DEFAULT_MAX_VISIBLE_RESULTS = 0;
        static private final double DEFAULT_LOAD_MORE_THRESHOLD = 0.82;
        static private final SearchSize DEFAULT_SIZE = SearchSize.SMALL;
        static private final SearchDisplayMode DEFAULT_DISPLAY_MODE = SearchDisplayMode.POPUP;
        static private final FieldVariant DEFAULT_VARIANT = FieldVariant.FILLED;

        private final Function<String, List<Entry<T>>> searchFunction;
        private Consumer<Entry<T>> onResultSelected;
        private String placeholderKey = DEFAULT_PLACEHOLDER_KEY;
        private String noResultKey = DEFAULT_NO_RESULT_KEY;
        private int debounceMs = DEFAULT_DEBOUNCE_MS;
        private int maxResults = DEFAULT_MAX_RESULTS;
        private int maxVisibleResults = DEFAULT_MAX_VISIBLE_RESULTS;
        private FieldVariant variant = DEFAULT_VARIANT;
        private SearchDisplayMode displayMode = DEFAULT_DISPLAY_MODE;
        private boolean searchOnTyping = true;
        private boolean showOnEmptyQuery = false;
        private double loadMoreThreshold = DEFAULT_LOAD_MORE_THRESHOLD;
        private SearchSize size = DEFAULT_SIZE;
        private Function<String, List<Entry<T>>> loadMoreFunction;

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

        public Builder<T> maxVisibleResults(int maxVisibleResults) {
            this.maxVisibleResults = maxVisibleResults;
            return this;
        }

        public Builder<T> searchOnTyping(boolean searchOnTyping) {
            this.searchOnTyping = searchOnTyping;
            return this;
        }

        public Builder<T> showOnEmptyQuery(boolean showOnEmptyQuery) {
            this.showOnEmptyQuery = showOnEmptyQuery;
            return this;
        }

        public Builder<T> loadMoreThreshold(double loadMoreThreshold) {
            if (loadMoreThreshold < 0 || loadMoreThreshold > 1) {
                throw new IllegalArgumentException("loadMoreThreshold must be between 0 and 1");
            }
            this.loadMoreThreshold = loadMoreThreshold;
            return this;
        }

        public Builder<T> size(SearchSize size) {
            this.size = size == null ? DEFAULT_SIZE : size;
            return this;
        }

        public Builder<T> displayMode(SearchDisplayMode displayMode) {
            this.displayMode = displayMode == null ? DEFAULT_DISPLAY_MODE : displayMode;
            return this;
        }

        public Builder<T> onLoadMore(Function<String, List<Entry<T>>> loadMoreFunction) {
            this.loadMoreFunction = loadMoreFunction;
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

        public Builder<T> variant(FieldVariant variant) {
            this.variant = variant;
            return this;
        }

        public Search<T> build() {
            return new Search<>(this);
        }
    }
}
