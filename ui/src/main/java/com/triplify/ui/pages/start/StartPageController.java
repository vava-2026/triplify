package com.triplify.ui.pages.start;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.net.URL;
import java.util.ResourceBundle;

public class StartPageController extends SimpleLifecycleAwareController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(StartPageController.class);

    @FXML private Label heroTitleLine1;
    @FXML private Label heroTitleLine2;
    @FXML private Label heroSubtitle;
    @FXML private HBox heroButtons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        heroTitleLine1.textProperty().bind(
                Bindings.createStringBinding(() -> I18n.t("start.title.line1"), I18n.bundleProperty()));
        heroTitleLine2.textProperty().bind(
                Bindings.createStringBinding(() -> I18n.t("start.title.line2"), I18n.bundleProperty()));
        heroSubtitle.textProperty().bind(
                Bindings.createStringBinding(() -> I18n.t("start.subtitle"), I18n.bundleProperty()));

        heroTitleLine2.getStyleClass().add("start-hero-title-tight");

        Button loginBtn = AppButtonView.builder()
                .variant(ButtonVariant.LOGIN)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("start.login"), I18n.bundleProperty()))
                .onAction(() -> log.debug("Log In clicked"))
                .build();

        Button signUpBtn = AppButtonView.builder()
                .variant(ButtonVariant.SIGN_UP)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("start.signUp"), I18n.bundleProperty()))
                .onAction(() -> log.debug("Sign Up clicked"))
                .build();

        heroButtons.getChildren().addAll(loginBtn, signUpBtn);
    }

    @Override
    public void onLifecycleShow() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(true);
        log.debug("StartPage shown – full screen mode enabled");
    }

    @Override
    public void onLifecycleHide() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(false);
        log.debug("StartPage hidden – full screen mode disabled");
    }

    @Override
    public void onLifecycleDestroy() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(false);
    }
}
