package com.triplify.ui.pages.license;

import com.google.inject.Inject;
import com.triplify.ui.routing.GuardedNavigator;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class LicenseExpiredController extends SimpleLifecycleAwareController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label bodyLabel;

    @FXML
    private Button accountButton;

    @Inject
    private GuardedNavigator navigator;

    @FXML
    private void initialize() {
        Localization.bindText(titleLabel.textProperty(), "page.licenseExpired.title");
        Localization.bindText(bodyLabel.textProperty(), "page.licenseExpired.body");
        Localization.bindText(accountButton.textProperty(), "page.licenseExpired.button");
        accountButton.getStyleClass().addAll("app-btn", "app-btn-primary", "app-btn-size-big");
        accountButton.setFocusTraversable(false);
    }

    @FXML
    public void onAccountClicked() {
        navigator.goTo(getRouter(), RouteIds.ACCOUNT);
    }
}

