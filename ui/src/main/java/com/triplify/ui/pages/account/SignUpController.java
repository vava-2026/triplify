package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.dto.SignUpRequest;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.result.Result;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.i18n.Language;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.routing.TriplifyRouterContext;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.checkbox_item.CheckboxItem;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.PasswordItem;
import com.triplify.ui.shared.menu.model.MenuItem;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class SignUpController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(SignUpController.class);
    private static final String ACTIVE_ROLE_BUTTON_CLASS = "signup-role-button-active";
    private static final String TERMS_ERROR_CLASS = "signup-terms-checkbox-error";

    @FXML private HBox signUpRoot;
    @FXML private Region authHero;
    @FXML private Region signUpFormPane;
    @FXML private VBox usernameInputContainer;
    @FXML private VBox emailInputContainer;
    @FXML private VBox passwordInputContainer;
    @FXML private VBox signUpButtonContainer;
    @FXML private HBox roleButtonsContainer;
    @FXML private VBox termsContainer;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label passwordLabel;
    @FXML private Label roleLabel;
    @FXML private Label haveAccountLabel;
    @FXML private Label goToLoginLabel;
    @FXML private Region signUpLanguageIsland;

    @Inject private AuthService authService;
    @Inject private ToastService toast;
    @Inject private UserSessionContext sessionContext;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private InputItem usernameInput;
    private InputItem emailInput;
    private PasswordItem passwordInput;
    private CheckboxItem termsCheckbox;
    private Button regularUserButton;
    private Button configManagerButton;
    private RoleEnum selectedRole = RoleEnum.USER;

    @FXML
    private void initialize() {
        authHero.prefWidthProperty().bind(signUpRoot.widthProperty().multiply(0.55));
        signUpFormPane.prefWidthProperty().bind(signUpRoot.widthProperty().multiply(0.45));

        if (signUpLanguageIsland != null) {
            signUpLanguageIsland.setOnMouseClicked(ignored -> {
                Language next = I18n.getLanguage() == Language.ENGLISH ? Language.SLOVAK : Language.ENGLISH;
                I18n.setLanguage(next);
            });
        }

        titleLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("signup.title"), I18n.bundleProperty()));
        subtitleLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("signup.subtitle"), I18n.bundleProperty()));
        usernameLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("signup.field.username"), I18n.bundleProperty()));
        emailLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("signup.field.email"), I18n.bundleProperty()));
        passwordLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("signup.field.password"), I18n.bundleProperty()));
        roleLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("signup.field.role"), I18n.bundleProperty()));
        haveAccountLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("signup.haveAccount"), I18n.bundleProperty()));
        goToLoginLabel.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("signup.goToLogin"), I18n.bundleProperty()));
        goToLoginLabel.setOnMouseClicked(ignored -> getRouter().moveto(RouteIds.LOGIN));

        usernameInput = new InputItem("signup.placeholder.username");
        emailInput = new InputItem("signup.placeholder.email");
        passwordInput = new PasswordItem("signup.placeholder.password");
        usernameInputContainer.getChildren().setAll(usernameInput);
        emailInputContainer.getChildren().setAll(emailInput);
        passwordInputContainer.getChildren().setAll(passwordInput);

        regularUserButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.USER)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("signup.role.regular"), I18n.bundleProperty()))
                .onAction(this::selectRegularUserRole)
                .build();
        regularUserButton.getStyleClass().add("signup-role-button");

        configManagerButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.USER)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("signup.role.configurationManager"), I18n.bundleProperty()))
                .onAction(this::selectConfigurationManagerRole)
                .build();
        configManagerButton.getStyleClass().add("signup-role-button");

        roleButtonsContainer.getChildren().setAll(regularUserButton, configManagerButton);
        HBox.setHgrow(regularUserButton, Priority.ALWAYS);
        HBox.setHgrow(configManagerButton, Priority.ALWAYS);
        regularUserButton.setMaxWidth(Double.MAX_VALUE);
        configManagerButton.setMaxWidth(Double.MAX_VALUE);
        setActiveRoleButton(regularUserButton);

        termsCheckbox = new CheckboxItem("");
        termsCheckbox.getStyleClass().add("signup-terms-checkbox");
        termsCheckbox.textProperty().bind(Bindings.createStringBinding(() -> I18n.t("signup.terms"), I18n.bundleProperty()));
        termsCheckbox.selectedProperty().addListener((ignoredObs, ignoredWasSelected, isSelected) -> {
            if (isSelected) {
                clearTermsError();
            }
        });
        termsContainer.getChildren().setAll(termsCheckbox);

        var signUpButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.LOGIN)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("signup.signUp"), I18n.bundleProperty()))
                .onAction(this::onSignUp)
                .build();
        signUpButton.getStyleClass().add("login-submit-button");
        signUpButton.setMaxWidth(Double.MAX_VALUE);
        signUpButtonContainer.getChildren().setAll(signUpButton);
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
        clearForm();
    }

    @Override
    public void onLifecycleDestroy() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(false);
        clearForm();
    }

    private void onSignUp() {
        attemptSignUp(usernameInput.getText().trim(), emailInput.getText().trim(), passwordInput.getText(), selectedRole);
    }

    private void attemptSignUp(String username, String email, String pass, RoleEnum role) {
        clearFieldErrors();

        SignUpRequest request = new SignUpRequest(username, email, pass, role, termsCheckbox.isSelected());
        Result<Void> result;
        try {
            result = authService.signUp(request);
        } catch (RuntimeException e) {
            log.error("Sign up failed due to unexpected runtime error", e);
            toast.error(I18n.t("error.infrastructure.database"));
            return;
        }
        result.onSuccess(ignored -> {
            var user = sessionContext.getCurrent().orElseThrow(() -> new IllegalStateException("User should be set in session after successful sign up"));
            log.info("Sign up successful for user '{}'", user.username());
            toast.success("Welcome, " + user.username() + "!");
            TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
            context.setSelectedMenuItem(MenuItem.MAP);
            getRouter().moveto(RouteIds.MAP);
        });
        result.onFailure(error -> errorHandler.handle(error, java.util.Map.of(
                "email", message -> emailInput.showError(message),
                "username", message -> usernameInput.showError(message),
                "password", message -> passwordInput.showError(message),
                "termsAccepted", message -> showTermsError()
        )));
    }

    private void clearFieldErrors() {
        usernameInput.clearError();
        emailInput.clearError();
        passwordInput.clearError();
        clearTermsError();
    }

    private void clearForm() {
        if (usernameInput != null) {
            usernameInput.setText("");
            usernameInput.clearError();
        }
        if (emailInput != null) {
            emailInput.setText("");
            emailInput.clearError();
        }
        if (passwordInput != null) {
            passwordInput.reset();
        }
        if (termsCheckbox != null) {
            termsCheckbox.setSelected(false);
        }
        selectedRole = RoleEnum.USER;
        if (regularUserButton != null && configManagerButton != null) {
            setActiveRoleButton(regularUserButton);
        }
        clearTermsError();
    }

    private void selectRegularUserRole() {
        selectedRole = RoleEnum.USER;
        setActiveRoleButton(regularUserButton);
        log.debug("Sign up role selected: regular user");
    }

    private void selectConfigurationManagerRole() {
        selectedRole = RoleEnum.CONFIGURATION_MANAGER;
        setActiveRoleButton(configManagerButton);
        log.debug("Sign up role selected: configuration manager");
    }

    private void setActiveRoleButton(Button activeButton) {
        regularUserButton.getStyleClass().remove(ACTIVE_ROLE_BUTTON_CLASS);
        configManagerButton.getStyleClass().remove(ACTIVE_ROLE_BUTTON_CLASS);

        if (!activeButton.getStyleClass().contains(ACTIVE_ROLE_BUTTON_CLASS)) {
            activeButton.getStyleClass().add(ACTIVE_ROLE_BUTTON_CLASS);
        }
    }

    private void showTermsError() {
        if (!termsCheckbox.getStyleClass().contains(TERMS_ERROR_CLASS)) {
            termsCheckbox.getStyleClass().add(TERMS_ERROR_CLASS);
        }
    }

    private void clearTermsError() {
        termsCheckbox.getStyleClass().remove(TERMS_ERROR_CLASS);
    }
}
