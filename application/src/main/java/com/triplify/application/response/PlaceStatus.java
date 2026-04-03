package com.triplify.application.response;

public enum PlaceStatus {

    VISITED("Visited",   "place-status-visited"),
    NOT_VISITED("Not visited", "place-status-not-visited"),
    PLANNED("Planned",   "place-status-planned"),
    SKIPPED("Skipped",   "place-status-skipped"),
    WISHLIST("Wishlist", "place-status-wishlist");

    private final String label;
    private final String cssClass;

    PlaceStatus(String label, String cssClass) {
        this.label    = label;
        this.cssClass = cssClass;
    }

    public String getLabel()    { return label; }
    public String getCssClass() { return cssClass; }

    public static PlaceStatus fromLabel(String label) {
        if (label == null) return null;
        for (PlaceStatus s : values()) {
            if (s.label.equalsIgnoreCase(label)) return s;
        }
        return null;
    }
}
