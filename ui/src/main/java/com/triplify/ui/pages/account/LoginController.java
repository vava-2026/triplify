package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.dto.LogInRequest;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.result.Result;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.i18n.Language;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.PasswordItem;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class LoginController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private HBox loginRoot;
    @FXML private Region authHero;
    @FXML private Region loginFormPane;
    @FXML private VBox usernameInputContainer;
    @FXML private VBox passwordInputContainer;
    @FXML private VBox loginButtonContainer;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label forgotPasswordLabel;
    @FXML private Label noAccountLabel;
    @FXML private Label createAccountLabel;
    @FXML private Region loginLanguageIsland;

    @Inject private AuthService authService;
    @Inject private ToastService toast;
    @Inject private UserSessionContext sessionContext;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private InputItem emailInput;
    private PasswordItem passwordInput;

    @FXML
    private void initialize() {
        authHero.prefWidthProperty().bind(loginRoot.widthProperty().multiply(0.55));
        loginFormPane.prefWidthProperty().bind(loginRoot.widthProperty().multiply(0.45));

        if (loginLanguageIsland != null) {
            loginLanguageIsland.setOnMouseClicked(event -> {
                Language next = I18n.getLanguage() == Language.ENGLISH ? Language.SLOVAK : Language.ENGLISH;
                I18n.setLanguage(next);
            });
        }

        titleLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("login.title"), I18n.bundleProperty()));
        subtitleLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("login.subtitle"), I18n.bundleProperty()));
        usernameLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("login.field.username"), I18n.bundleProperty()));
        passwordLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("login.field.password"), I18n.bundleProperty()));
        forgotPasswordLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("login.forgotPassword"), I18n.bundleProperty()));
        noAccountLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("login.noAccount"), I18n.bundleProperty()));
        createAccountLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("login.createAccount"), I18n.bundleProperty()));
        createAccountLabel.setOnMouseClicked(event -> getRouter().moveto(RouteIds.SIGN_UP));

        emailInput = new InputItem("login.placeholder.username");
        passwordInput = new PasswordItem("login.placeholder.password");
        usernameInputContainer.getChildren().setAll(emailInput);
        passwordInputContainer.getChildren().setAll(passwordInput);

        var loginButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.LOGIN)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("login.signIn"), I18n.bundleProperty()))
                .onAction(this::onLogin)
                .build();
        loginButton.getStyleClass().add("login-submit-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButtonContainer.getChildren().setAll(loginButton);
    }

    @Override
    public void onLifecycleShow() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(true);
    }

    @Override
    public void onLifecycleHide() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(false);
    }

    @Override
    public void onLifecycleDestroy() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(false);
    }

    private void onLogin() {
        attemptLogin(emailInput.getText().trim(), passwordInput.getText());
    }

    private void attemptLogin(String email, String pass) {
        clearFieldErrors();

        LogInRequest request = new LogInRequest(email, pass);
        Result<Void> result = authService.login(request);
        result.onSuccess(ignored -> {
            var user = sessionContext.getCurrent().orElseThrow(() -> new IllegalStateException("User should be set in session after successful login"));
            log.info("Login successful for user '{}'", user.username());
            toast.success("Welcome back, " + user.username() + "!");
        });
        result.onFailure(error -> errorHandler.handle(error, java.util.Map.of(
                "email", message -> emailInput.showError(message),
                "username", message -> emailInput.showError(message),
                "password", message -> passwordInput.showError(message)
        )));
    }

    private void clearFieldErrors() {
        emailInput.clearError();
        passwordInput.clearError();
    }
}
