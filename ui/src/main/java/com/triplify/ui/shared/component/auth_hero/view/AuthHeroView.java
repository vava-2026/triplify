package com.triplify.ui.shared.component.auth_hero.view;

import com.triplify.ui.i18n.I18n;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class AuthHeroView implements Initializable {

    @FXML private Label brandTitle;
    @FXML private Label heroTitleLine1;
    @FXML private Label heroTitleLine2;
    @FXML private Label descriptionLine1;
    @FXML private Label descriptionLine2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        brandTitle.setText("TRIPLIFY");
        heroTitleLine1.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("auth.hero.title.line1"), I18n.bundleProperty()));
        heroTitleLine2.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("auth.hero.title.line2"), I18n.bundleProperty()));
        descriptionLine1.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("auth.hero.description.line1"), I18n.bundleProperty()));
        descriptionLine2.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("auth.hero.description.line2"), I18n.bundleProperty()));
    }
}
