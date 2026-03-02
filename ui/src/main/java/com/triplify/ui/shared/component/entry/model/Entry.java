package com.triplify.ui.shared.component.entry.model;

public class Entry<T> {

    private final T value;
    private final String label;
    private final String iconLiteral;
    private final EntryVariant variant;

    private Entry(Builder<T> b) {
        this.value = b.value;
        this.label = b.label;
        this.iconLiteral = b.iconLiteral;
        this.variant = b.variant;
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

    public EntryVariant getVariant() {
        return variant;
    }

    public boolean hasIcon() {
        return iconLiteral != null && !iconLiteral.isBlank();
    }

    public boolean hasVariant() {
        return variant != null;
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
        private EntryVariant variant;

        private Builder(T value, String label) {
            this.value = value;
            this.label = label;
        }

        public Builder<T> icon(String iconLiteral) {
            this.iconLiteral = iconLiteral;
            return this;
        }

        public Builder<T> variant(EntryVariant v) {
            this.variant = v;
            return this;
        }

        public Entry<T> build() {
            return new Entry<>(this);
        }
    }
}
