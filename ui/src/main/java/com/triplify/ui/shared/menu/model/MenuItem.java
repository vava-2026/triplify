package com.triplify.ui.shared.menu.model;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.RouteIds;

public enum MenuItem {

    MAP("nav.map", RouteIds.MAP, true),
    MY_TRIPS("nav.myTrips", RouteIds.MY_TRIPS, false),
    CALENDAR("nav.calendar", RouteIds.CALENDAR, false),
    SETTINGS("nav.settings", RouteIds.SETTINGS, false),
    ACCOUNT("nav.account", RouteIds.LOGIN, false); // TODO: change to account, login just for example here 

    private final String i18nKey;
    private final String routeId;
    private final boolean hideHeader;

    MenuItem(String i18nKey, String routeId, boolean hideHeader) {
        this.i18nKey = i18nKey;
        this.routeId = routeId;
        this.hideHeader = hideHeader;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public String getRouteId() {
        return routeId;
    }

    public boolean isHideHeader() {
        return hideHeader;
    }

    public String getLabel() {
        return I18n.t(i18nKey);
    }
}
