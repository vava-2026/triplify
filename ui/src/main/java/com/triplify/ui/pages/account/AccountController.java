package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.auth.dto.SignUpRequest;
import com.triplify.application.usecase.badge.BadgeService;
import com.triplify.application.usecase.badge.dto.BadgeResponse;
import com.triplify.application.usecase.badge.dto.GetBadgesRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.result.Result;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.GuardedNavigator;
import com.triplify.ui.routing.RouteIds;
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

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AccountController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private static final int BADGE_COLUMNS = 5;

    @FXML private VBox editFormContainer;
    @FXML private GridPane badgesGrid;

    @Inject private ToastService toast;
    @Inject private AuthService authService;
    @Inject private BadgeService badgeService;
    @Inject private UserSessionContext userSessionContext;
    @Inject private ErrorHandler errorHandler;
    @Inject private GuardedNavigator guardedNavigator;

    private InputItem usernameInput;
    private InputItem emailInput;
    private PasswordItem passwordInput;

    @FXML
    public void initialize() {
        render();
        loadBadges();
    }

    @Override
    public void onLifecycleShow() {
        loadBadges();
    }

    private void loadBadges() {
        badgesGrid.getChildren().clear();
        try {
            Result<List<BadgeResponse>> result = badgeService.getBadges(new GetBadgesRequest(null));
            result.onSuccess(this::renderBadges);
            result.onFailure(error -> {
                log.warn("Failed to load badges for account page: {}", error.message());
                errorHandler.handle(error);
            });
        } catch (Exception ex) {
            log.error("Unexpected error while loading badges for account page", ex);
        }
    }

    private void renderBadges(List<BadgeResponse> badges) {
        badgesGrid.getChildren().clear();

        for (int i = 0; i < badges.size(); i++) {
            BadgeResponse response = badges.get(i);
            BadgeView badgeView = new BadgeView();
            badgeView.update(toUiBadge(response));

            int col = i % BADGE_COLUMNS;
            int row = i / BADGE_COLUMNS;
            badgesGrid.add(badgeView, col, row);
        }
    }

    private Badge toUiBadge(BadgeResponse response) {
        int requiredValue = response.requiredValue();
        int currentValue = 100;
        boolean unlocked = requiredValue <= 100;

        return new Badge(
                response.name(),
                response.nameSk(),
                response.description(),
                response.descriptionSk(),
                resolveImageUrl(response.image()),
                mapGroup(response),
                response.level(),
                requiredValue,
                currentValue,
                unlocked
        );
    }

    private BadgeGroup mapGroup(BadgeResponse response) {
        String groupName = response.group() != null ? response.group().name() : null;
        if (groupName == null || groupName.isBlank()) {
            return BadgeGroup.COUNTRIES;
        }

        return switch (groupName.trim().toLowerCase(Locale.ROOT)) {
            case "countries" -> BadgeGroup.COUNTRIES;
            case "kilometers" -> BadgeGroup.KILOMETERS;
            case "trips" -> BadgeGroup.TRIPS;
            case "routes" -> BadgeGroup.ROUTES;
            case "places" -> BadgeGroup.PLACES;
            case "stories" -> BadgeGroup.STORIES;
            case "photos" -> BadgeGroup.PHOTOS;
            default -> BadgeGroup.COUNTRIES;
        };
    }

    private String resolveImageUrl(ImageResponse image) {
        if (image == null || image.url() == null) {
            return null;
        }

        String rawPath = image.url().toString().replace("\\", "/");
        String fileName = rawPath.substring(rawPath.lastIndexOf('/') + 1);

        URL classpathUrl = getClass().getResource("/com/triplify/ui/shared/component/badge/images/" + fileName);
        if (classpathUrl != null) {
            return classpathUrl.toExternalForm();
        }

        Path path = image.url();
        if (path.isAbsolute()) {
            return path.toUri().toString();
        }

        return rawPath;
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
                guardedNavigator.goTo(getRouter(), RouteIds.START);
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
        if (usernameInput == null || emailInput == null || passwordInput == null) {
            log.warn("Ignoring save action because account edit inputs are not initialized for current state");
            return;
        }

        clearErrors();

        String rawPassword = passwordInput.getText();
        SignUpRequest request = new SignUpRequest(
                usernameInput.getText().trim(),
                emailInput.getText().trim(),
                rawPassword,
                RoleEnum.USER,
                true
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
        if (usernameInput != null) {
            usernameInput.clearError();
        }
        if (emailInput != null) {
            emailInput.clearError();
        }
        if (passwordInput != null) {
            passwordInput.clearError();
        }
    }
}
