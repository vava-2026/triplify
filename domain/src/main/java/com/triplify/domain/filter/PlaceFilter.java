package com.triplify.domain.filter;

public record PlaceFilter(
        String name,
        String countryId
) {
    public PlaceFilter {
        name = name == null ? null : name.trim();
        countryId = countryId == null ? null : countryId.trim();
    }
}
