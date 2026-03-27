package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.dto.LogInRequest;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.result.Result;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.shared.toast.ToastService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.Map;

// TODO: Remove, this is just for example
public class LoginController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private static final String ERROR_STYLE_CLASS = "input-error";

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label usernameError;
    @FXML private Label passwordError;

    @Inject private AuthService authService;
    @Inject private ToastService toast;
    @Inject private UserSessionContext sessionContext;
    @Inject private ErrorHandler errorHandler;

    @FXML
    public void onLogin() {
        attemptLogin(username.getText().trim(), password.getText());
    }

    private void attemptLogin(String username, String pass) {
        clearFieldErrors();

        LogInRequest request = new LogInRequest(username, pass);

        Map<String, TextField> fieldMap = Map.of("username", this.username, "password", password);
        clearFieldStyles(fieldMap);

        Result<Void> result = authService.login(request);
        result.onSuccess(auth -> {
            var user = sessionContext.getCurrent().orElseThrow(() -> new IllegalStateException("User should be set in session after successful login"));
            log.info("Login successful for user '{}'", user.username());
            toast.success("Welcome back, " + user.username() + "!");
        });
        result.onFailure(error -> errorHandler.handle(error, Map.of(
                "username", message -> {
                    markFieldError(this.username);
                    showFieldError(usernameError, message);
                },
                "password", message -> {
                    markFieldError(this.password);
                    showFieldError(passwordError, message);
                }
        )));
    }

    private void clearFieldErrors() {
        hideFieldError(usernameError);
        hideFieldError(passwordError);
    }

    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideFieldError(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    private void clearFieldStyles(Map<String, TextField> fieldMap) {
        fieldMap.values().forEach(field -> field.getStyleClass().remove(ERROR_STYLE_CLASS));
    }

    private void markFieldError(TextField field) {
        if (!field.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            field.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }
}
