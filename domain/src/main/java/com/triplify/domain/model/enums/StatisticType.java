package com.triplify.domain.model.enums;

import lombok.Getter;

@Getter
public enum StatisticType {
    COUNTRIES_VISITED("statistics.type.countriesVisited", true, "fth-globe", "icon-green", "10000000-0000-0000-0000-000000000001"),
    TOTAL_TRIPS("statistics.type.totalTrips", true, "fth-navigation", "icon-blue", "10000000-0000-0000-0000-000000000003"),
    PLACES_VISITED("statistics.type.placesVisited", true, "fth-triangle", "icon-yellow", "10000000-0000-0000-0000-000000000005"),
    TRAVEL_DAYS("statistics.type.travelDays", true, "fth-calendar", "icon-purple", null),
    PHOTOS_UPLOADED("statistics.type.photosUploaded", true, "fth-image", "icon-pink", "10000000-0000-0000-0000-000000000007"),
    KILOMETERS_TRAVELLED("statistics.type.kilometersTravelled", false, null, null, "10000000-0000-0000-0000-000000000002"),
    ROUTES_CREATED("statistics.type.routesCreated", false, null, null, "10000000-0000-0000-0000-000000000004"),
    STORIES_CREATED("statistics.type.storiesCreated", false, null, null, "10000000-0000-0000-0000-000000000006");

    private final String labelKey;
    private final boolean isDisplayed;
    private final String icon;
    private final String iconColor;
    private final String badgeGroupId;

    StatisticType(String labelKey, boolean isDisplayed, String icon, String iconColor, String badgeGroupId) {
        this.labelKey = labelKey;
        this.isDisplayed = isDisplayed;
        this.icon = icon;
        this.iconColor = iconColor;
        this.badgeGroupId = badgeGroupId;
    }
}
