package com.triplify.ui.shared.component.auth_hero.view;

import com.triplify.ui.shared.util.Localization;
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
        Localization.bindText(heroTitleLine1.textProperty(), "auth.hero.title.line1");
        Localization.bindText(heroTitleLine2.textProperty(), "auth.hero.title.line2");
        Localization.bindText(descriptionLine1.textProperty(), "auth.hero.description.line1");
        Localization.bindText(descriptionLine2.textProperty(), "auth.hero.description.line2");
    }
}
