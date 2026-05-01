package com.triplify.ui.routing;

import com.triplify.domain.model.enums.RoleEnum;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum AppPage {
    START(
            RouteIds.START,
            "page.start",
            "fth-home",
            EnumSet.noneOf(RoleEnum.class),
            false,
            true,
            false,
            false,
            true,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    LOGIN(
            RouteIds.LOGIN,
            "page.login",
            "fth-log-in",
            EnumSet.noneOf(RoleEnum.class),
            false,
            true,
            false,
            false,
            true,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    SIGN_UP(
            RouteIds.SIGN_UP,
            "page.signUp",
            "fth-user-plus",
            EnumSet.noneOf(RoleEnum.class),
            false,
            true,
            false,
            false,
            true,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    MAP(
            RouteIds.MAP,
            "nav.map",
            "fth-map",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            true,
            false,
            true,
            null,
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            false,
            false
    ),
    MY_PLACES(
            RouteIds.MY_PLACES,
            "nav.myPlaces",
            "fth-map-pin",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            true,
            false
    ),
    MY_ROUTES(
            RouteIds.MY_ROUTES,
            "nav.myRoutes",
            "fth-navigation",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            true,
            false
    ),
    MY_TRIPS(
            RouteIds.MY_TRIPS,
            "nav.myTrips",
            "fth-briefcase",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    MY_IMAGES(
            RouteIds.MY_IMAGES,
            "nav.myImages",
            "fth-image",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    TRIP_DETAILS(
            RouteIds.TRIP_DETAILS,
            "page.tripDetails",
            "fth-briefcase",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            false,
            false,
            false,
            RouteIds.MY_TRIPS,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    ADD_PLACE(
            RouteIds.ADD_PLACE,
            "page.addPlace",
            "fth-plus-circle",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            false,
            false,
            false,
            RouteIds.MY_TRIPS,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    STORY_DETAILS(
            RouteIds.STORY_DETAILS,
            "story.details.fallback.title",
            "fth-book-open",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            false,
            false,
            false,
            RouteIds.MY_TRIPS,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    ADD_STORY(
            RouteIds.ADD_STORY,
            "story.add.page.title",
            "fth-edit-3",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            false,
            false,
            false,
            RouteIds.MY_TRIPS,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    MY_BADGES(
            RouteIds.MY_BADGES,
            "nav.myBadges",
            "fth-award",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            true,
            false
    ),
    MY_STATISTICS(
            RouteIds.MY_STATISTICS,
            "nav.myStatistics",
            "fth-bar-chart-2",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    CALENDAR(
            RouteIds.CALENDAR,
            "nav.calendar",
            "fth-calendar",
            EnumSet.of(RoleEnum.PRO_USER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            true
    ),
    COUNTRIES(
            RouteIds.COUNTRIES,
            "nav.countries",
            "fth-globe",
            EnumSet.of(RoleEnum.CONFIGURATION_MANAGER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.of(RoleEnum.CONFIGURATION_MANAGER),
            true,
            false
    ),
    CATEGORIES(
            RouteIds.CATEGORIES,
            "nav.categories",
            "fth-layers",
            EnumSet.of(RoleEnum.CONFIGURATION_MANAGER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            true,
            false
    ),
    EMOTIONS(
            RouteIds.EMOTIONS,
            "nav.emotions",
            "fth-smile",
            EnumSet.of(RoleEnum.CONFIGURATION_MANAGER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            true,
            false
    ),
    BADGES(
            RouteIds.BADGES,
            "nav.badges",
            "fth-award",
            EnumSet.of(RoleEnum.CONFIGURATION_MANAGER),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            true,
            false
    ),
    SETTINGS(
            RouteIds.SETTINGS,
            "nav.settings",
            "fth-settings",
            EnumSet.allOf(RoleEnum.class),
            true,
            false,
            true,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    ACCOUNT(
            RouteIds.ACCOUNT,
            "nav.account",
            "fth-user",
            EnumSet.allOf(RoleEnum.class),
            true,
            false,
            false,
            true,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    ),
    LICENSE_EXPIRED(
            RouteIds.LICENSE_EXPIRED,
            "page.licenseExpired",
            "fth-alert-triangle",
            EnumSet.of(RoleEnum.USER, RoleEnum.PRO_USER),
            true,
            false,
            false,
            false,
            false,
            null,
            EnumSet.noneOf(RoleEnum.class),
            false,
            false
    );

    private static final Map<String, AppPage> BY_ROUTE_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(AppPage::getRouteId, page -> page));

    private final String routeId;
    private final String labelKey;
    private final String icon;
    private final EnumSet<RoleEnum> allowedRoles;
    private final boolean requiresAuth;
    private final boolean guestOnly;
    private final boolean showInPrimaryMenu;
    private final boolean showInAccountSection;
    private final boolean hideHeader;
    private final String activeMenuRouteId;
    private final EnumSet<RoleEnum> defaultForRoles;
    private final boolean placeholder;
    private final boolean pro;

    AppPage(
            String routeId,
            String labelKey,
            String icon,
            EnumSet<RoleEnum> allowedRoles,
            boolean requiresAuth,
            boolean guestOnly,
            boolean showInPrimaryMenu,
            boolean showInAccountSection,
            boolean hideHeader,
            String activeMenuRouteId,
            EnumSet<RoleEnum> defaultForRoles,
            boolean placeholder,
            boolean pro
    ) {
        this.routeId = routeId;
        this.labelKey = labelKey;
        this.icon = icon;
        this.allowedRoles = allowedRoles.clone();
        this.requiresAuth = requiresAuth;
        this.guestOnly = guestOnly;
        this.showInPrimaryMenu = showInPrimaryMenu;
        this.showInAccountSection = showInAccountSection;
        this.hideHeader = hideHeader;
        this.activeMenuRouteId = activeMenuRouteId;
        this.defaultForRoles = defaultForRoles.clone();
        this.placeholder = placeholder;
        this.pro = pro;
    }

    public EnumSet<RoleEnum> getAllowedRoles() {
        return allowedRoles.clone();
    }

    public String getRouteId() {
        return routeId;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public String getIcon() {
        return icon;
    }

    public boolean requiresAuth() {
        return requiresAuth;
    }

    public boolean isGuestOnly() {
        return guestOnly;
    }

    public boolean isHideHeader() {
        return hideHeader;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public boolean isPro() {
        return pro;
    }

    public boolean isAllowedFor(RoleEnum role) {
        return allowedRoles.contains(role);
    }

    public AppPage getActivePrimaryPage() {
        if (activeMenuRouteId == null) {
            return showInPrimaryMenu ? this : null;
        }
        return fromRouteId(activeMenuRouteId).orElse(null);
    }

    public boolean isDefaultFor(RoleEnum role) {
        return defaultForRoles.contains(role);
    }

    public static Optional<AppPage> fromRouteId(String routeId) {
        return Optional.ofNullable(BY_ROUTE_ID.get(routeId));
    }

    public static AppPage defaultForRole(RoleEnum role) {
        return Arrays.stream(values())
                .filter(page -> page.isDefaultFor(role))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No default page configured for role " + role));
    }

    public static List<AppPage> primaryMenuPagesFor(RoleEnum role) {
        return filterPages(page -> page.showInPrimaryMenu && page.isAllowedFor(role));
    }

    public static List<AppPage> accountPagesFor(RoleEnum role) {
        return filterPages(page -> page.showInAccountSection && page.isAllowedFor(role));
    }

    private static List<AppPage> filterPages(Predicate<AppPage> predicate) {
        return Arrays.stream(values())
                .filter(predicate)
                .toList();
    }
}
