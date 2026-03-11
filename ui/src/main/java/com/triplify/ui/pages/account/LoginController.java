package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.error.ValidationMapper;
import com.triplify.application.error.ValidationResult;
import com.triplify.application.result.Result;
import com.triplify.application.usecase.auth.AuthResponse;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.LoginRequest;
import com.triplify.ui.shared.error.ErrorPresenter;
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

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label errorBanner;
    @FXML private Label usernameError;
    @FXML private Label passwordError;

    @Inject private AuthService authService;
    @Inject private ToastService toast;

    @FXML
    public void onLogin() {
        attemptLogin(username.getText().trim(), password.getText());
    }

    private void attemptLogin(String user, String pass) {
        clearFieldErrors();

        LoginRequest command = new LoginRequest(user, pass);

        Map<String, TextField> fieldMap = Map.of("username", username, "password", password);
        Map<String, Label> errorMap = Map.of("username", usernameError, "password", passwordError);

        ValidationResult<LoginRequest> validation = ValidationMapper.validate(command);
        if (validation.isFailure()) {
            ErrorPresenter.showValidation(validation, fieldMap);
            validation.getViolations().forEach(v -> {
                Label label = errorMap.get(v.getField());
                if (label != null) showFieldError(label, v.getMessageKey());
            });
            return;
        }

        // Step 2 - use-case
        Result<AuthResponse> result = authService.login(command);
        result.onSuccess(auth -> {
            log.info("Login successful for user '{}'", auth.username());
            toast.success("Welcome back, " + auth.username() + "!");})
              .onFailureResponse(errors -> {
                  log.info("Login failed: {}", result.getFirstErrorResponse().messageKey());
                  toast.error(ErrorPresenter.resolveKey(result.getFirstErrorResponse().messageKey()));
              });
    }

    private void clearFieldErrors() {
        hideFieldError(usernameError);
        hideFieldError(passwordError);
    }

    private void showFieldError(Label label, String messageKey) {
        label.setText(ErrorPresenter.resolveKey(messageKey));
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideFieldError(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }
}
