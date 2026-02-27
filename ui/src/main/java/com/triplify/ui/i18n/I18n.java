package com.triplify.ui.i18n;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public final class I18n {

    private static final String BUNDLE_BASE = "messages";

    private record State(Language language, ResourceBundle bundle) {
    }

    private static final AtomicReference<State> STATE =
            new AtomicReference<>(loadState(Language.ENGLISH));

    private I18n() {
    }

    private static State loadState(Language language) {
        return new State(language, ResourceBundle.getBundle(BUNDLE_BASE, language.getLocale()));
    }

    /**
     * Switch language using the {@link Language} enum.
     * Atomic — no locks needed.
     */
    public static void setLanguage(Language language) {
        STATE.set(loadState(language));
    }

    /**
     * Get the currently active language.
     */
    public static Language getLanguage() {
        return STATE.get().language();
    }

    /**
     * Expose the bundle directly (useful for FXMLLoader).
     */
    public static ResourceBundle getBundle() {
        return STATE.get().bundle();
    }

    /**
     * Translate a key.
     */
    public static String t(String key) {
        return STATE.get().bundle().getString(key);
    }
}
