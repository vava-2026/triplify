package com.triplify.domain.model.enums;

public enum TripPlaceSourceType {
    MANUAL("manual"),
    ROUTE("route");

    private final String value;

    TripPlaceSourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TripPlaceSourceType fromValue(String value) {
        for (TripPlaceSourceType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown trip place source type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
