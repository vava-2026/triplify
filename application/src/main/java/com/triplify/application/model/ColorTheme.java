package com.triplify.application.model;

import com.triplify.domain.model.enums.ColorEnum;

public enum ColorTheme {

    GRAY("app-theme-gray"),
    RED("app-theme-red"),
    ORANGE("app-theme-orange"),
    YELLOW("app-theme-yellow"),
    GREEN("app-theme-green"),
    TEAL("app-theme-teal"),
    BLUE("app-theme-blue"),
    PURPLE("app-theme-purple"),
    PINK("app-theme-pink");

    private final String styleClass;

    ColorTheme(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public static ColorTheme from(ColorEnum color) {
        return switch (color) {
            case GRAY -> GRAY;
            case RED -> RED;
            case ORANGE -> ORANGE;
            case YELLOW -> YELLOW;
            case GREEN -> GREEN;
            case TEAL -> TEAL;
            case BLUE -> BLUE;
            case PURPLE -> PURPLE;
            case PINK -> PINK;
        };
    }
}

