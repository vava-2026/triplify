package com.triplify.ui.shared.component.entry.model;

public enum EntryVariant {

    PRIMARY("app-entry-primary"),

    SECONDARY("app-entry-secondary"),

    DANGER("app-entry-danger");

    private final String styleClass;

    EntryVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getStyleClass() {
        return styleClass;
    }
}

