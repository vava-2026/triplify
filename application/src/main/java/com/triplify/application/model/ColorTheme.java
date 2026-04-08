package com.triplify.application.model;

import com.triplify.domain.model.enums.ColorEnum;

public enum ColorTheme {

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

    public ColorEnum toColorEnum() {
        return switch (this) {
            case RED -> ColorEnum.RED;
            case ORANGE -> ColorEnum.ORANGE;
            case YELLOW -> ColorEnum.YELLOW;
            case GREEN -> ColorEnum.GREEN;
            case TEAL -> ColorEnum.TEAL;
            case BLUE -> ColorEnum.BLUE;
            case PURPLE -> ColorEnum.PURPLE;
            case PINK -> ColorEnum.PINK;
        };
    }
}

