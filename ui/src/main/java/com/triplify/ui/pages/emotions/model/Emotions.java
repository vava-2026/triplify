package com.triplify.ui.pages.emotions.model;

import com.triplify.application.usecase.emotion.EmotionService;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.domain.error.AppError;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Emotions {

    private static final String DEFAULT_PLACEHOLDER_KEY = "input.placeholder.emotion";
    private static final String DEFAULT_NO_RESULT_KEY = "search.noResult";
    private static final int DEFAULT_DEBOUNCE_MS = 50;

    private final EmotionService emotionService;
    private final Consumer<Entry<String>> onResultSelected;
    private final Consumer<AppError> onLoadFailed;

    @Getter private final StringProperty placeholder = new SimpleStringProperty();
    @Getter private final StringProperty noResult = new SimpleStringProperty();
    @Getter private final String placeholderKey;
    @Getter private final String noResultKey;
    @Getter private final int debounceMs;
    @Getter private final FieldVariant variant;
    @Getter private final boolean searchOnTyping;

    private List<EmotionResponse> allEmotions;

    private Emotions(Builder builder) {
        this.emotionService = builder.emotionService;
        this.onResultSelected = builder.onResultSelected;
        this.onLoadFailed = builder.onLoadFailed;
        this.placeholderKey = builder.placeholderKey;
        this.noResultKey = builder.noResultKey;
        this.debounceMs = builder.debounceMs;
        this.variant = builder.variant;
        this.searchOnTyping = builder.searchOnTyping;

        Localization.bindText(placeholder, builder.placeholderKey);
        Localization.bindText(noResult, builder.noResultKey);
    }

    public List<Entry<String>> search(String query) {
        ensureLoaded();
        return filter(query == null ? "" : query.trim());
    }

    public void selectResult(Entry<String> result) {
        if (onResultSelected != null) {
            onResultSelected.accept(result);
        }
    }

    public Entry<String> findById(String id) {
        if (id == null || id.isBlank()) return null;
        ensureLoaded();
        if (allEmotions == null) return null;
        return allEmotions.stream()
                .filter(e -> e.id() != null && e.id().toString().equals(id))
                .findFirst()
                .map(this::toEntry)
                .orElse(null);
    }

    private void ensureLoaded() {
        if (allEmotions != null) return;
        var result = emotionService.getAllEmotions();
        result.onSuccess(list -> allEmotions = list);
        result.onFailure(error -> {
            allEmotions = List.of();
            if (onLoadFailed != null) onLoadFailed.accept(error);
        });
    }

    private List<Entry<String>> filter(String query) {
        if (allEmotions == null) return List.of();
        return allEmotions.stream()
                .filter(e -> query.isBlank() || matches(e, query))
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    private boolean matches(EmotionResponse e, String query) {
        String lower = query.toLowerCase();
        return (e.name() != null && e.name().toLowerCase().contains(lower))
                || (e.nameSk() != null && e.nameSk().toLowerCase().contains(lower));
    }

    private Entry<String> toEntry(EmotionResponse e) {
        return Entry.<String>builder(e.id().toString(), Localization.localizedBinding(e))
                .emoji(e.emojiUnicode())
                .build();
    }

    public static Builder builder(EmotionService emotionService) {
        return new Builder(emotionService);
    }

    public static class Builder {

        private final EmotionService emotionService;
        private Consumer<Entry<String>> onResultSelected;
        private Consumer<AppError> onLoadFailed;
        private String placeholderKey = DEFAULT_PLACEHOLDER_KEY;
        private String noResultKey = DEFAULT_NO_RESULT_KEY;
        private int debounceMs = DEFAULT_DEBOUNCE_MS;
        private FieldVariant variant = FieldVariant.GHOST;
        private boolean searchOnTyping = true;

        private Builder(EmotionService emotionService) {
            if (emotionService == null) throw new IllegalArgumentException("emotionService must not be null");
            this.emotionService = emotionService;
        }

        public Builder placeholderKey(String key) { this.placeholderKey = key; return this; }
        public Builder noResultKey(String key) { this.noResultKey = key; return this; }
        public Builder debounceMs(int ms) { this.debounceMs = ms; return this; }
        public Builder variant(FieldVariant variant) { this.variant = variant; return this; }
        public Builder searchOnTyping(boolean value) { this.searchOnTyping = value; return this; }
        public Builder onResultSelected(Consumer<Entry<String>> cb) { this.onResultSelected = cb; return this; }
        public Builder onLoadFailed(Consumer<AppError> cb) { this.onLoadFailed = cb; return this; }

        public Emotions build() { return new Emotions(this); }
    }
}
