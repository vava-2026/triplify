package com.triplify.ui.shared.util;

import com.triplify.application.shared.localization.LocalizedDescription;
import com.triplify.application.shared.localization.LocalizedName;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.i18n.Language;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;

import java.util.function.Supplier;

public final class Localization {

    private Localization() {}

    public static StringBinding textBinding(String key) {
        return Bindings.createStringBinding(() -> I18n.t(key), I18n.bundleProperty());
    }

    public static void bindText(StringProperty property, String key) {
        property.bind(textBinding(key));
    }

    public static String localize(String english, String slovak) {
        return I18n.getLanguage() == Language.SLOVAK
                ? prefer(slovak, english)
                : prefer(english, slovak);
    }

    public static String localize(LocalizedName localizedName) {
        return localizedName == null ? "" : localize(localizedName.name(), localizedName.nameSk());
    }

    public static String localizeDescription(LocalizedDescription localizedDescription) {
        return localizedDescription == null
                ? ""
                : localize(localizedDescription.description(), localizedDescription.descriptionSk());
    }

    public static StringBinding localizedBinding(Supplier<String> english, Supplier<String> slovak) {
        return Bindings.createStringBinding(
                () -> localize(english.get(), slovak.get()),
                I18n.languageProperty()
        );
    }

    public static StringBinding localizedBinding(LocalizedName localizedName) {
        return localizedName == null
                ? localizedBinding(() -> "", () -> "")
                : localizedBinding(localizedName::name, localizedName::nameSk);
    }

    public static StringBinding localizedDescriptionBinding(LocalizedDescription localizedDescription) {
        return localizedDescription == null
                ? localizedBinding(() -> "", () -> "")
                : localizedBinding(localizedDescription::description, localizedDescription::descriptionSk);
    }

    public static void bindLocalizedText(StringProperty property, Supplier<String> english, Supplier<String> slovak) {
        property.bind(localizedBinding(english, slovak));
    }

    public static void bindLocalizedText(StringProperty property, LocalizedName localizedName) {
        if (localizedName == null) {
            property.unbind();
            property.set("");
            return;
        }
        bindLocalizedText(property, localizedName::name, localizedName::nameSk);
    }

    public static void bindLocalizedDescription(StringProperty property, LocalizedDescription localizedDescription) {
        if (localizedDescription == null) {
            property.unbind();
            property.set("");
            return;
        }
        property.bind(localizedDescriptionBinding(localizedDescription));
    }

    private static String prefer(String primary, String fallback) {
        if (hasText(primary)) {
            return primary;
        }
        if (hasText(fallback)) {
            return fallback;
        }
        return "";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
