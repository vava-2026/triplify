package com.triplify.ui.i18n;

import java.util.ResourceBundle;

public final class I18n {

    private static final String BUNDLE_BASE = "messages";
    private static Language currentLanguage = Language.ENGLISH;
    private static ResourceBundle bundle = load(currentLanguage);

    private I18n() { }

    private static ResourceBundle load(Language language) {
        return ResourceBundle.getBundle(BUNDLE_BASE, language.getLocale());
    }

    /**
     * Switch language using the {@link Language} enum.
     */
    public static void setLanguage(Language language) {
        currentLanguage = language;
        bundle = load(language);
    }

    /**
     * Get the currently active language.
     */
    public static Language getLanguage() {
        return currentLanguage;
    }

    /**
     * Expose the bundle directly (useful for FXMLLoader).
     */
    public static ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Translate a key.
     */
    public static String t(String key) {
        return bundle.getString(key);
    }
}
