package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.error.ValidationMapper;
import com.triplify.application.error.ValidationResult;
import com.triplify.application.usecase.account.UpdateProfileRequest;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.PasswordItem;
import com.triplify.ui.shared.component.input_item.TextAreaItem;
import com.triplify.ui.shared.component.input_item.model.InputVariant;
import com.triplify.ui.shared.toast.ToastService;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class AccountController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @FXML private VBox editFormContainer;

    @Inject private ToastService toast;

    private InputItem nameInput;
    private InputItem emailInput;
    private PasswordItem passwordInput;
    private TextAreaItem bioInput;

    @FXML
    public void initialize() {
        nameInput = new InputItem("input.placeholder.fullName", InputVariant.FILLED);
        emailInput = new InputItem("input.placeholder.email");
        passwordInput = new PasswordItem("input.placeholder.password", InputVariant.GHOST);
        bioInput = new TextAreaItem("input.placeholder.bio");

        editFormContainer.getChildren().addAll(nameInput, emailInput, passwordInput, bioInput);
    }

    @FXML
    private void onSave() {
        clearErrors();

        String rawPassword = passwordInput.getText();
        UpdateProfileRequest request = new UpdateProfileRequest(
                nameInput.getText().trim(),
                emailInput.getText().trim(),
                rawPassword.isBlank() ? null : rawPassword,
                bioInput.getText().trim()
        );

        ValidationResult<UpdateProfileRequest> validation = ValidationMapper.validate(request);
        if (validation.isFailure()) {
            validation.getViolations().forEach(v -> {
                String msg = I18n.t(v.getMessageKey());
                switch (v.getField()) {
                    case "name" -> nameInput.showError(msg);
                    case "email" -> emailInput.showError(msg);
                    case "newPassword" -> passwordInput.showError(msg);
                    case "bio" -> bioInput.showError(msg);
                    default -> log.warn("No input mapped for violated field '{}'", v.getField());
                }
            });
            return;
        }

        // TODO: call AccountService once available
        log.info("Profile update validated successfully");
        toast.success(I18n.t("account.profile.saved"));
    }

    private void clearErrors() {
        nameInput.clearError();
        emailInput.clearError();
        passwordInput.clearError();
        bioInput.clearError();
    }
}


