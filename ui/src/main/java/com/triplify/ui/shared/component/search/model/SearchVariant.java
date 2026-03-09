package com.triplify.ui.shared.component.search.model;

import lombok.Getter;

@Getter
public enum SearchVariant {

    WHITE("app-search-variant-filled"),
    BLUE("app-search-variant-blue"),
    OUTLINED("app-search-variant-outlined");

    private final String styleClass;

    SearchVariant(String styleClass) {
        this.styleClass = styleClass;
    }
}
