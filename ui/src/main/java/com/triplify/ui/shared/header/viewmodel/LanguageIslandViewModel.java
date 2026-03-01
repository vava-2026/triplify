package com.triplify.ui.shared.header.viewmodel;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.i18n.Language;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;

public class LanguageIslandViewModel {

    private final StringBinding languageCode = Bindings.createStringBinding(
            () -> I18n.t("language.code"),
            I18n.bundleProperty());

    public StringBinding languageCodeBinding() { return languageCode; }

    public void toggle() {
        Language next = I18n.getLanguage() == Language.ENGLISH
                ? Language.SLOVAK
                : Language.ENGLISH;
        I18n.setLanguage(next);
    }
}

