package com.triplify.ui.shared.component.button.model;

public enum ButtonVariant {

    PRIMARY("app-btn-primary"),

    SECONDARY("app-btn-secondary"),

    DANGER("app-btn-danger"),

    GHOST("app-btn-ghost"),

    LOGIN("app-btn-login"),

    SIGN_UP("app-btn-sign-up");


    private final String styleClass;

    ButtonVariant(String styleClass) { this.styleClass = styleClass; }

    public String getStyleClass() { return styleClass; }
}

