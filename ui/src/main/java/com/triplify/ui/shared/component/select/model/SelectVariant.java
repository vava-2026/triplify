package com.triplify.ui.shared.component.select.model;

public enum SelectVariant {

    PRIMARY("app-select-primary"),

    SECONDARY("app-select-secondary"),

    DANGER("app-select-danger"),

    GHOST("app-select-ghost");

    private final String styleClass;

    SelectVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getStyleClass() {
        return styleClass;
    }
}

