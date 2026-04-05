package com.triplify.domain.model.enums;

public enum ColorEnum {
    GRAY("gray"),
    RED("red"),
    ORANGE("orange"),
    YELLOW("yellow"),
    GREEN("green"),
    TEAL("teal"),
    BLUE("blue"),
    PURPLE("purple"),
    PINK("pink");

    private final String value;

    ColorEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

