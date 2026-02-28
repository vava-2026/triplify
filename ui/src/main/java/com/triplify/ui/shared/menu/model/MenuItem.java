package com.triplify.ui.shared.menu.model;

import com.triplify.ui.i18n.I18n;

public enum MenuItem {

    MAP("nav.map"),
    MY_TRIPS("nav.myTrips"),
    CALENDAR("nav.calendar"),
    SETTINGS("nav.settings"),
    ACCOUNT("nav.account");

    private final String i18nKey;

    MenuItem(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public String getLabel() {
        return I18n.t(i18nKey);
    }
}
