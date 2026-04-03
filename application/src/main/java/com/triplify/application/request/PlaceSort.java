package com.triplify.application.request;

public enum PlaceSort {

    NEWEST_FIRST("Newest first"),
    OLDEST_FIRST("Oldest first"),
    NAME_AZ("Name A → Z"),
    NAME_ZA("Name Z → A");

    private final String label;

    PlaceSort(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
