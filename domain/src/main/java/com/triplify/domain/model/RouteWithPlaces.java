package com.triplify.domain.model;

import java.util.List;

public record RouteWithPlaces(
        Route route,
        List<RoutePlace> routePlaces
) {
}
