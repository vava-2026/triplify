package com.triplify.ui.shared.component.select.entry.model;

import com.triplify.application.model.ColorTheme;

public class Entry<T> {

    private final T value;
    private final String label;
    private final String iconLiteral;
    private final ColorTheme colorTheme;
    private final String emoji;

    private Entry(Builder<T> b) {
        this.value = b.value;
        this.label = b.label;
        this.iconLiteral = b.iconLiteral;
        this.colorTheme = b.colorTheme;
        this.emoji = b.emoji;
    }

    public T getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getIconLiteral() {
        return iconLiteral;
    }

    public boolean hasIcon() {
        return iconLiteral != null && !iconLiteral.isBlank();
    }

    public ColorTheme getColorTheme() {
        return colorTheme;
    }

    public boolean hasColorTheme() {
        return colorTheme != null;
    }

    public String getEmoji() {
        return emoji;
    }

    public boolean hasEmoji() {
        return emoji != null && !emoji.isBlank();
    }

    @Override
    public String toString() {
        return label;
    }

    public static <T> Builder<T> builder(T value, String label) {
        return new Builder<>(value, label);
    }

    public static final class Builder<T> {
        private final T value;
        private final String label;
        private String iconLiteral;
        private ColorTheme colorTheme;
        private String emoji;

        private Builder(T value, String label) {
            this.value = value;
            this.label = label;
        }

        public Builder<T> icon(String iconLiteral) {
            this.iconLiteral = iconLiteral;
            return this;
        }

        public Builder<T> colorTheme(ColorTheme theme) {
            this.colorTheme = theme;
            return this;
        }

        public Builder<T> emoji(String emoji) {
            this.emoji = emoji;
            return this;
        }

        public Entry<T> build() {
            return new Entry<>(this);
        }
    }
}
