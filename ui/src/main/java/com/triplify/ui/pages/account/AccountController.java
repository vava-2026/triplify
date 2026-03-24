package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.error.ValidationMapper;
import com.triplify.application.error.ValidationResult;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.SignUpRequest;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.PasswordItem;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class AccountController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @FXML private VBox editFormContainer;

    @Inject private ToastService toast;
    @Inject private AuthService authService;
    @Inject private UserSessionContext userSessionContext;

    private InputItem usernameInput;
    private InputItem emailInput;
    private PasswordItem passwordInput;

    @FXML
    public void initialize() {
        render();
    }

    private void render() {
        editFormContainer.getChildren().clear();
        if (userSessionContext.isLoggedIn()) {
            SessionUser user = userSessionContext.getCurrent().orElseThrow();
            Label usernameLabel = new Label("Username: " + user.username());
            Button logOffButton = new Button("Log off");
            logOffButton.setOnAction(e -> {
                authService.logout();
                toast.success("Logged off successfully");
                render();
            });
            editFormContainer.getChildren().addAll(usernameLabel, logOffButton);
        } else {
            usernameInput = new InputItem("Username", FieldVariant.FILLED);
            emailInput = new InputItem("input.placeholder.email");
            passwordInput = new PasswordItem("input.placeholder.password", FieldVariant.GHOST);

            editFormContainer.getChildren().addAll(usernameInput, emailInput, passwordInput);
        }
    }

    @FXML
    private void onSave() {
        if (userSessionContext.isLoggedIn()) {
            return;
        }

        clearErrors();

        String rawPassword = passwordInput.getText();
        SignUpRequest request = new SignUpRequest(
                usernameInput.getText().trim(),
                emailInput.getText().trim(),
                rawPassword,
                RoleEnum.USER
        );

        ValidationResult<SignUpRequest> validation = ValidationMapper.validate(request);
        if (validation.isFailure()) {
            validation.getViolations().forEach(v -> {
                String msg = I18n.t(v.getMessageKey());
                switch (v.getField()) {
                    case "username" -> usernameInput.showError(msg);
                    case "email" -> emailInput.showError(msg);
                    case "password" -> passwordInput.showError(msg);
                    default -> log.warn("No input mapped for violated field '{}'", v.getField());
                }
            });
            return;
        }

        authService.signUp(request).onSuccess(v -> {
            toast.success("Signed up successfully");
            render();
        }).onFailure(errors -> {
            if (!errors.isEmpty()) {
                toast.error(I18n.t(errors.getFirst().messageKey()));
            }
        });
    }

    private void clearErrors() {
        if (usernameInput != null) usernameInput.clearError();
        if (emailInput != null) emailInput.clearError();
        if (passwordInput != null) passwordInput.clearError();
    }
}
