package com.triplify.ui.shared.menu.model;

import com.triplify.ui.i18n.I18n;

public enum MenuItem {

    MAP("nav.map", true),
    MY_TRIPS("nav.myTrips", false),
    CALENDAR("nav.calendar", false),
    SETTINGS("nav.settings", false),
    ACCOUNT("nav.account", false);

    private final String i18nKey;
    private final boolean hideHeader;

    MenuItem(String i18nKey, boolean hideHeader) {
        this.i18nKey = i18nKey;
        this.hideHeader = hideHeader;
    }

    public String getI18nKey() { return i18nKey; }
    public boolean isHideHeader() { return hideHeader; }
    public String getLabel() { return I18n.t(i18nKey); }
}
