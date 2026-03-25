package com.triplify.domain.model.enums;

public enum StatusEnum {
    PLANNED("planned"),
    ONGOING("ongoing"),
    VISITED("visited"),
    CANCELED("canceled");

    private final String value;

    StatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static StatusEnum fromValue(String value) throws IllegalArgumentException {
        for (StatusEnum status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
