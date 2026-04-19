package com.triplify.application.usecase.trip.dto;

public enum TripStatus {
    VISITED("Visited", "trip-status-visited"),
    DRAFTED("Drafted", "trip-status-drafted"),
    REJECTED("Rejected", "trip-status-rejected"),
    PLANNED("Planned", "trip-status-planned"),
    ONGOING("Ongoing", "trip-status-ongoing");

    private final String label;
    private final String cssClass;

    TripStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public static TripStatus fromLabel(String label) {
        if (label == null || label.isBlank()) return null;
        for (TripStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}
