package com.triplify.ui.shared.component.select.model;

public enum SelectVariant {

    FILLED("app-select-variant-filled"),
    OUTLINED("app-select-variant-outlined");

    private final String styleClass;

    SelectVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
