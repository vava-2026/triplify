package com.triplify.ui.i18n;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public final class I18n {

    private static final Logger logger = LoggerFactory.getLogger(I18n.class);
    private static final String BUNDLE_BASE = "messages";

    private static final ObjectProperty<Language> languageProperty =
            new SimpleObjectProperty<>(Language.ENGLISH);

    private static final ObjectProperty<ResourceBundle> bundleProperty =
            new SimpleObjectProperty<>(loadBundle(Language.ENGLISH));

    static {
        languageProperty.addListener((obs, oldLang, newLang) -> {
            bundleProperty.set(loadBundle(newLang));
            logger.info("Language switched to: {}", newLang);
        });
    }

    private I18n() {}
    public static ReadOnlyObjectProperty<Language> languageProperty() {
        return languageProperty;
    }

    public static Language getLanguage() {
        return languageProperty.get();
    }

    public static void setLanguage(Language language) {
        languageProperty.set(language);
    }

    public static ReadOnlyObjectProperty<ResourceBundle> bundleProperty() {
        return bundleProperty;
    }

    public static ResourceBundle getBundle() {
        return bundleProperty.get();
    }

    public static String t(String key) {
        return bundleProperty.get().getString(key);
    }

    private static ResourceBundle loadBundle(Language language) {
        return ResourceBundle.getBundle(BUNDLE_BASE, language.getLocale());
    }
}
