package com.triplify.domain.model.enums;

public enum StatusEnum {
    PLANNED("planned", "Planned", "trip-status-planned"),
    ONGOING("ongoing", "Ongoing", "trip-status-ongoing"),
    VISITED("visited", "Visited", "trip-status-visited"),
    CANCELED("canceled", "Canceled", "trip-status-canceled");

    private final String value;
    private final String label;
    private final String cssClass;

    StatusEnum(String value, String label, String cssClass) {
        this.value = value;
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public static StatusEnum fromValue(String value) throws IllegalArgumentException {
        for (StatusEnum status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }

    public static StatusEnum fromLabel(String label) {
        if (label == null || label.isBlank()) return null;
        for (StatusEnum status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
