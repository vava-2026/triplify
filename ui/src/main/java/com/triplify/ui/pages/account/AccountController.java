package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.dto.SignUpRequest;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.result.Result;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.badge.model.Badge;
import com.triplify.ui.shared.component.badge.model.BadgeGroup;
import com.triplify.ui.shared.component.badge.view.BadgeView;
import com.triplify.ui.shared.component.input_item.InputItem;
import com.triplify.ui.shared.component.input_item.PasswordItem;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.toast.ToastService;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.Map;

public class AccountController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @FXML private VBox editFormContainer;
    @FXML private GridPane badgesGrid;

    @Inject private ToastService toast;
    @Inject private AuthService authService;
    @Inject private UserSessionContext userSessionContext;
    @Inject private ErrorHandler errorHandler;

    private InputItem usernameInput;
    private InputItem emailInput;
    private PasswordItem passwordInput;

    @FXML
    public void initialize() {
        render();

        editFormContainer.getChildren().addAll(nameInput, emailInput, passwordInput, bioInput);

        BadgeView badgeView = new BadgeView();
        badgeView.update(new Badge("Super Traveler", "Awarded for completing 10 trips", null, BadgeGroup.RED, 1, 10, 5, false));
        BadgeView badgeView2 = new BadgeView();
        badgeView2.update(new Badge("Super Traveler2", "Awarded for completing 10 trips", null, BadgeGroup.RED, 1, 10, 6, true));
        badgesGrid.add(badgeView, 0, 0);
        badgesGrid.add(badgeView2, 1, 0);
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
            usernameInput = new InputItem("input.placeholder.username", FieldVariant.FILLED);
            emailInput = new InputItem("input.placeholder.email");
            passwordInput = new PasswordItem("input.placeholder.password", FieldVariant.GHOST);

            editFormContainer.getChildren().addAll(usernameInput, emailInput, passwordInput);
        }
    }

    @FXML
    private void onSave() {
        clearErrors();

        String rawPassword = passwordInput.getText();
        SignUpRequest request = new SignUpRequest(
                usernameInput.getText().trim(),
                emailInput.getText().trim(),
                rawPassword,
                RoleEnum.USER
        );

        Result<Void> result = authService.signUp(request);
        result.onSuccess(v -> {
            log.info("Profile updated successfully");
            toast.success(I18n.t("account.profile.saved"));
            render();
        });
        result.onFailure(error -> errorHandler.handle(error, Map.of(
                "username", message -> {
                    this.usernameInput.showError(message);
                },
                "email", message -> {
                    this.emailInput.showError(message);
                },
                "password", message -> {
                    this.passwordInput.showError(message);
                }
        )));
    }

    private void clearErrors() {
        usernameInput.clearError();
        emailInput.clearError();
        passwordInput.clearError();
    }
}
