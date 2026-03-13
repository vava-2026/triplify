package com.triplify.domain.model.enums;

public enum ColorEnum {
    RED("red"),
    ORANGE("orange"),
    YELLOW("yellow"),
    GREEN("green"),
    BLUE("blue"),
    PURPLE("purple"),
    PINK("pink"),
    GRAY("gray");

    private final String value;

    ColorEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ColorEnum fromValue(String value) throws IllegalArgumentException {
        for (ColorEnum color : values()) {
            if (color.value.equalsIgnoreCase(value)) {
                return color;
            }
        }
        throw new IllegalArgumentException("Unknown color: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
