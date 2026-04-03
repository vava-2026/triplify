package com.triplify.ui.shared.component.countries.model;

import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.country.dto.GetCountriesRequest;
import com.triplify.domain.error.AppError;
import com.triplify.domain.filter.CountryFilter;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Countries {

    private static final String DEFAULT_PLACEHOLDER_KEY = "input.placeholder.country";
    private static final String DEFAULT_NO_RESULT_KEY = "search.noResult";
    private static final int DEFAULT_DEBOUNCE_MS = 50;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CountryService countryService;
    private final Consumer<Entry<String>> onResultSelected;
    private final Consumer<AppError> onLoadFailed;

    @Getter private final StringProperty placeholder = new SimpleStringProperty();
    @Getter private final StringProperty noResult = new SimpleStringProperty();
    @Getter private final String placeholderKey;
    @Getter private final String noResultKey;
    @Getter private final int debounceMs;
    @Getter private final int pageSize;
    @Getter private final FieldVariant variant;
    @Getter private final boolean searchOnTyping;

    private final List<Entry<String>> entries = new ArrayList<>();

    private String activeQuery = "";
    private int nextPage = 0;
    private boolean hasMore = true;
    private boolean loading = false;

    private Countries(Builder builder) {
        this.countryService = builder.countryService;
        this.onResultSelected = builder.onResultSelected;
        this.onLoadFailed = builder.onLoadFailed;
        this.placeholderKey = builder.placeholderKey;
        this.noResultKey = builder.noResultKey;
        this.debounceMs = builder.debounceMs;
        this.pageSize = builder.pageSize;
        this.variant = builder.variant;
        this.searchOnTyping = builder.searchOnTyping;

        Localization.bindText(placeholder, builder.placeholderKey);
        Localization.bindText(noResult, builder.noResultKey);
    }

    public List<Entry<String>> search(String query) {
        String normalized = normalize(query);
        if (!normalized.equals(activeQuery)) {
            resetState(normalized);
        }
        if (entries.isEmpty() && nextPage == 0) {
            loadNextPage();
        }
        return List.copyOf(entries);
    }

    public List<Entry<String>> loadMore(String query) {
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

    public boolean loadNextPage() {
        if (loading || !hasMore) {
            return false;
        }

        loading = true;
        int loadedCount = 0;

        try {
            var request = new GetCountriesRequest(
                    new PageRequest(nextPage, pageSize),
                    new CountryFilter(activeQuery, CountryFilter.CountryBanFilter.ONLY_UNBANNED, false)
            );

            var result = countryService.getCountries(request);
            result.onSuccess(page -> {
                for (var country : page.items()) {
                    entries.add(Entry.<String>builder(country.id(), country.name()).emoji(country.emojiUnicode()).build());
                }
            });
            result.onFailure(error -> {
                hasMore = false;
                if (onLoadFailed != null) {
                    onLoadFailed.accept(error);
                }
            });

            if (result.isSuccess()) {
                loadedCount = result.getValue().items().size();
                hasMore = result.getValue().hasNext();
                nextPage++;
            }
        } finally {
            loading = false;
        }

        return loadedCount > 0;
    }

    public void selectResult(Entry<String> result) {
        if (onResultSelected != null && result != null) {
            onResultSelected.accept(result);
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

    public static Builder builder(CountryService countryService) {
        return new Builder(countryService);
    }

    public static class Builder {

        private final CountryService countryService;

        private Consumer<Entry<String>> onResultSelected;
        private Consumer<AppError> onLoadFailed;
        private String placeholderKey = DEFAULT_PLACEHOLDER_KEY;
        private String noResultKey = DEFAULT_NO_RESULT_KEY;
        private int debounceMs = DEFAULT_DEBOUNCE_MS;
        private int pageSize = DEFAULT_PAGE_SIZE;
        private FieldVariant variant = FieldVariant.FILLED;
        private boolean searchOnTyping = true;

        private Builder(CountryService countryService) {
            if (countryService == null) {
                throw new IllegalArgumentException("countryService must not be null");
            }
            this.countryService = countryService;
        }

        public Builder placeholderKey(String placeholderKey) {
            this.placeholderKey = placeholderKey;
            return this;
        }

        public Builder noResultKey(String noResultKey) {
            this.noResultKey = noResultKey;
            return this;
        }

        public Builder debounceMs(int debounceMs) {
            this.debounceMs = debounceMs;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder variant(FieldVariant variant) {
            this.variant = variant;
            return this;
        }

        public Builder searchOnTyping(boolean searchOnTyping) {
            this.searchOnTyping = searchOnTyping;
            return this;
        }

        public Builder onResultSelected(Consumer<Entry<String>> onResultSelected) {
            this.onResultSelected = onResultSelected;
            return this;
        }

        public Builder onLoadFailed(Consumer<AppError> onLoadFailed) {
            this.onLoadFailed = onLoadFailed;
            return this;
        }

        public Countries build() {
            return new Countries(this);
        }
    }
}








