package com.triplify.ui.shared.component.button.model;

import lombok.Getter;

@Getter
public enum ButtonVariant {

    PRIMARY("app-btn-primary"),
    SECONDARY("app-btn-secondary"),
    DANGER("app-btn-danger"),
    DANGER_OUTLINE("app-btn-danger-outline"),
    GHOST("app-btn-ghost"),
    PRO("app-btn-pro"),
    WHITE("app-btn-white"),
    USER("app-btn-user");


    private final String styleClass;

    ButtonVariant(String styleClass) { this.styleClass = styleClass; }

}
