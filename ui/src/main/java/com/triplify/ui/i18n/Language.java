package com.triplify.ui.i18n;

import java.util.Locale;

public enum Language {

    ENGLISH("English", Locale.ENGLISH),
    SLOVAK("Slovenčina", Locale.forLanguageTag("sk"));

    private final String displayName;
    private final Locale locale;

    Language(String displayName, Locale locale) {
        this.displayName = displayName;
        this.locale = locale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
