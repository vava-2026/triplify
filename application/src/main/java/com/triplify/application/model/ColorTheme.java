package com.triplify.application.model;

import com.triplify.domain.model.enums.ColorEnum;
import lombok.Getter;

@Getter
public enum ColorTheme {

    GRAY("app-theme-gray"),
    RED_DARK("app-theme-red-dark"),
    RED("app-theme-red"),
    ROSE("app-theme-rose"),
    ORANGE("app-theme-orange"),
    AMBER("app-theme-amber"),
    YELLOW("app-theme-yellow"),
    GOLDEN_BROWN("app-theme-golden-brown"),
    LIME("app-theme-lime"),
    GREEN("app-theme-green"),
    INDIGO("app-theme-indigo"),
    VIOLET("app-theme-violet"),
    STEEL_BLUE("app-theme-steel-blue"),
    TEAL("app-theme-teal"),
    BLUE("app-theme-blue"),
    CYAN("app-theme-cyan"),
    SAGE("app-theme-sage"),
    BROWN("app-theme-brown"),
    PURPLE("app-theme-purple"),
    PINK("app-theme-pink");

    private final String styleClass;

    ColorTheme(String styleClass) {
        this.styleClass = styleClass;
    }

    public static ColorTheme from(ColorEnum color) {
        return color == null ? GRAY : ColorTheme.valueOf(color.name());
    }

    public ColorEnum toColorEnum() {
        return ColorEnum.valueOf(name());
    }

    public static ColorTheme forLabel(String label) {
        ColorTheme[] values = ColorTheme.values();
        return values[Math.floorMod(label == null ? 0 : label.hashCode(), values.length)];
    }
}

