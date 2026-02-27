package com.triplify.ui.shared.menu.model;

/**
 * Represents a navigable item in the sidebar menu.
 * Add new entries here to extend the navigation.
 */
public enum MenuItem {

    MAP("Map"),
    MY_TRIPS("My Trips"),
    CALENDAR("Calendar"),
    SETTINGS("Settings");

    private final String label;

    MenuItem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

