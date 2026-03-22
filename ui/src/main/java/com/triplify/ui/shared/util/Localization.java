package com.triplify.ui.shared.util;

import com.triplify.ui.i18n.I18n;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;

public class Localization {
    public static void bindText(StringProperty property, String key) {
        property.bind(
                Bindings.createStringBinding(() -> I18n.t(key), I18n.bundleProperty())
        );
    }
}