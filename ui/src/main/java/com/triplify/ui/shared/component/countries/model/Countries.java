package com.triplify.ui.shared.component.countries.model;

import com.triplify.application.usecase.country.CountryService;
import com.triplify.application.usecase.country.dto.GetCountriesRequest;
import com.triplify.application.usecase.country.dto.CountryResponse;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
                    entries.add(Entry.<String>builder(country.id().toString(), country.name()).emoji(country.emojiUnicode()).build());
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

    public Entry<String> findBestMatch(String externalCountryName) {
        for (String candidate : candidateQueries(externalCountryName)) {
            Entry<String> match = findExactMatch(candidate);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private Entry<String> findExactMatch(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }

        Set<String> normalizedCandidates = new LinkedHashSet<>();
        normalizedCandidates.add(normalizeCountryName(candidate));

        var request = new GetCountriesRequest(
                new PageRequest(0, Math.max(pageSize, 20)),
                new CountryFilter(candidate, CountryFilter.CountryBanFilter.ONLY_UNBANNED, false)
        );

        var result = countryService.getCountries(request);
        if (!result.isSuccess()) {
            if (onLoadFailed != null) {
                result.onFailure(onLoadFailed);
            }
            return null;
        }

        for (CountryResponse country : result.getValue().items()) {
            String normalizedName = normalizeCountryName(country.name());
            String normalizedNameSk = normalizeCountryName(country.nameSk());
            if (normalizedCandidates.contains(normalizedName) || normalizedCandidates.contains(normalizedNameSk)) {
                return Entry.<String>builder(country.id().toString(), country.name()).emoji(country.emojiUnicode()).build();
            }
        }

        return null;
    }

    private List<String> candidateQueries(String externalCountryName) {
        if (externalCountryName == null || externalCountryName.isBlank()) {
            return List.of();
        }

        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(externalCountryName.trim());

        switch (externalCountryName.trim()) {
            case "United States of America" -> candidates.add("United States");
            case "Czech Republic" -> candidates.add("Czechia");
            case "Republic of the Congo" -> candidates.add("Congo");
            case "Democratic Republic of the Congo" -> candidates.add("Democratic Republic of Congo");
            case "Ivory Coast" -> candidates.add("Cote d'Ivoire");
            case "Macedonia" -> candidates.add("North Macedonia");
            case "Cape Verde" -> candidates.add("Cabo Verde");
            case "East Timor" -> candidates.add("Timor-Leste");
            case "Swaziland" -> candidates.add("Eswatini");
            default -> {
            }
        }

        return List.copyOf(candidates);
    }

    private String normalizeCountryName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("’", "'")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
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








