package com.triplify.ui.shared.menu.model;

public enum NavItem {

    MAP (MenuItem.MAP, "fth-map"),
    MY_TRIPS (MenuItem.MY_TRIPS, "fth-briefcase"),
    CALENDAR (MenuItem.CALENDAR, "fth-calendar"),
    SETTINGS (MenuItem.SETTINGS, "fth-settings");

    private final MenuItem menuItem;
    private final String icon;

    NavItem(MenuItem menuItem, String icon) {
        this.menuItem = menuItem;
        this.icon = icon;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public String getIcon() { return icon; }
    public String getI18nKey() { return menuItem.getI18nKey(); }
}

