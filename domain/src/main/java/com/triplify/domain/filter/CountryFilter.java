package com.triplify.domain.filter;

public record CountryFilter(
        String name,
        CountryBanFilter banFilter,
        boolean hasTripOrPlace
) {
    public enum CountryBanFilter {
        ALL,
        ONLY_BANNED,
        ONLY_UNBANNED
    }

    public CountryFilter {
        name = name == null ? null : name.trim();
        banFilter = banFilter == null ? CountryBanFilter.ALL : banFilter;
        hasTripOrPlace = false;
    }
}
