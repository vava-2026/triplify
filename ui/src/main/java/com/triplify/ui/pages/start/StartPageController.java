package com.triplify.ui.pages.start;

import com.google.inject.Inject;
import com.triplify.ui.routing.GuardedNavigator;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.beans.binding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.net.URL;
import java.util.ResourceBundle;

public class StartPageController extends SimpleLifecycleAwareController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(StartPageController.class);
    private static final double HERO_BUTTONS_CONTAINER_WIDTH = 320;

    @Inject private FxmlLoaderHelper fxmlLoader;
    @Inject private GuardedNavigator guardedNavigator;

    @FXML private Label heroTitleLine1;
    @FXML private Label heroTitleLine2;
    @FXML private Label heroSubtitle;
    @FXML private HBox heroButtons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Localization.bindText(heroTitleLine1.textProperty(), "start.title.line1");
        Localization.bindText(heroTitleLine2.textProperty(), "start.title.line2");
        Localization.bindText(heroSubtitle.textProperty(), "start.subtitle");

        heroTitleLine2.getStyleClass().add("start-hero-title-tight");

        heroButtons.setMinWidth(HERO_BUTTONS_CONTAINER_WIDTH);
        heroButtons.setPrefWidth(HERO_BUTTONS_CONTAINER_WIDTH);
        heroButtons.setMaxWidth(HERO_BUTTONS_CONTAINER_WIDTH);

        Button loginBtn = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("start.login"))
                .size(AppComponentSize.BIG)
                .onAction(() -> {
                    log.debug("Log In clicked");
                    guardedNavigator.goTo(getRouter(), RouteIds.LOGIN);
                })
                .build();

        Button signUpBtn = AppButtonView.builder(fxmlLoader)
                .labelBinding(Localization.textBinding("start.signUp"))
                .variant(ButtonVariant.WHITE)
                .size(AppComponentSize.BIG)
                .onAction(() -> {
                    log.debug("Sign Up clicked");
                    guardedNavigator.goTo(getRouter(), RouteIds.SIGN_UP);
                })
                .build();

        var halfWidth = Bindings.createDoubleBinding(
                () -> Math.max(0, (heroButtons.getWidth() - heroButtons.getSpacing()) / 2.0),
                heroButtons.widthProperty(),
                heroButtons.spacingProperty()
        );

        loginBtn.prefWidthProperty().bind(halfWidth);
        loginBtn.maxWidthProperty().bind(halfWidth);
        signUpBtn.prefWidthProperty().bind(halfWidth);
        signUpBtn.maxWidthProperty().bind(halfWidth);

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
