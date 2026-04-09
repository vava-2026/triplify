package com.triplify.domain.model.enums;

public enum ColorEnum {
    GRAY("gray"),
    RED_DARK("red_dark"),
    RED("red"),
    ROSE("rose"),
    ORANGE("orange"),
    AMBER("amber"),
    YELLOW("yellow"),
    GOLDEN_BROWN("golden_brown"),
    LIME("lime"),
    GREEN("green"),
    INDIGO("indigo"),
    VIOLET("violet"),
    STEEL_BLUE("steel_blue"),
    TEAL("teal"),
    BLUE("blue"),
    CYAN("cyan"),
    SAGE("sage"),
    BROWN("brown"),
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

