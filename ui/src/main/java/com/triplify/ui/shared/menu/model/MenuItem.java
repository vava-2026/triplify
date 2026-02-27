package com.triplify.ui.shared.menu.model;

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

