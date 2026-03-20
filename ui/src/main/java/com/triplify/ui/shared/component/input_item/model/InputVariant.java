package com.triplify.ui.shared.component.input_item.model;

public enum InputVariant {

    OUTLINED("input-item--outlined"),
    FILLED("input-item--filled"),
    GHOST("input-item--ghost");

    private final String styleClass;

    InputVariant(String styleClass) { this.styleClass = styleClass; }

    public String getStyleClass() { return styleClass; }
}
