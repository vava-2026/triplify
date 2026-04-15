package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.usecase.auth.AuthService;
import com.triplify.application.usecase.badge.BadgeService;
import com.triplify.application.usecase.badge.dto.BadgeResponse;
import com.triplify.application.usecase.badge.dto.GetBadgesRequest;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.GetImageByIdRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.result.Result;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.PageAccessService;
import com.triplify.ui.routing.GuardedNavigator;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.button.model.ButtonVariant;
import com.triplify.ui.shared.component.button.view.AppButtonView;
import com.triplify.ui.shared.component.badge.viewmodel.BadgeViewModel;
import com.triplify.ui.shared.component.badge.view.BadgeView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.AvatarImageHelper;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class AccountController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private static final int BADGE_COLUMNS = 5;

    @FXML private GridPane badgesGrid;
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileRolePill;
    @FXML private Label profileAvatarInitial;
    @FXML private ImageView profileAvatarImage;
    @FXML private StackPane upgradeButtonContainer;
    @FXML private StackPane logoutButtonContainer;

    @Inject private ToastService toast;
    @Inject private AuthService authService;
    @Inject private BadgeService badgeService;
    @Inject private ImageService imageService;
    @Inject private UserSessionContext userSessionContext;
    @Inject private ErrorHandler errorHandler;
    @Inject private GuardedNavigator guardedNavigator;
    @Inject private PageAccessService pageAccessService;
    @Inject private FxmlLoaderHelper fxmlLoader;

    @FXML
    public void initialize() {
        setupLogoutButton();
        refreshProfileHero();
        loadBadges();
    }

    private void setupLogoutButton() {
        Button logoutButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.PRIMARY)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("account.logout"), I18n.bundleProperty()))
                .icon("fth-log-out")
                .onAction(this::onLogOut)
                .build();

        logoutButton.setFocusTraversable(false);
        logoutButton.getStyleClass().add("profile-logout-btn");
        logoutButtonContainer.getChildren().setAll(logoutButton);
    }

    @Override
    public void onLifecycleShow() {
        refreshProfileHero();
        loadBadges();
    }

    private void refreshProfileHero() {
        if (userSessionContext.isLoggedIn()) {
            SessionUser user = userSessionContext.getCurrent().orElseThrow();
            renderProfileHero(user);
        } else {
            clearProfileHero();
        }
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

    private BadgeViewModel toUiBadge(BadgeResponse response) {
        int requiredValue = response.requiredValue();
        int currentValue = 100;
        boolean unlocked = requiredValue <= 100;

        return new BadgeViewModel(
                response.name(),
                response.nameSk(),
                response.description(),
                response.descriptionSk(),
                resolveImageUrl(response.image()),
                response.group(),
                response.level(),
                requiredValue,
                currentValue,
                unlocked
        );
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

    private void renderProfileHero(SessionUser user) {
        profileNameLabel.setText(user.username());
        profileEmailLabel.setText(user.email() == null || user.email().isBlank() ? "youremail@gmail.com" : user.email());

        String roleLabelKey = pageAccessService.getRoleLabelKey(user.role());
        profileRolePill.textProperty().unbind();
        profileRolePill.textProperty().bind(Bindings.createStringBinding(
            () -> I18n.t(roleLabelKey),
            I18n.bundleProperty()));

        profileAvatarInitial.setText(AvatarImageHelper.extractInitial(user.username()));
        profileAvatarImage.setClip(new Circle(75, 75, 75));
        applyAvatarImage(null);
        renderUpgradeSection(user.role());

        if (user.avatarImageId() != null) {
            var avatarResult = imageService.getImageById(new GetImageByIdRequest(user.avatarImageId().toString()));
            avatarResult.onSuccess(image -> applyAvatarImage(image.url()));
            avatarResult.onFailure(error -> {
                log.debug("Avatar image not available for account hero '{}': {}", user.username(), error.message());
                applyAvatarImage(null);
            });
        }
    }

    private void renderUpgradeSection(RoleEnum role) {
        if (role == RoleEnum.CONFIGURATION_MANAGER) {
            upgradeButtonContainer.getChildren().clear();
            return;
        }

        Button upgradeButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.PRO)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("account.upgrade"), I18n.bundleProperty()))
                .build();

        upgradeButton.setFocusTraversable(false);
        upgradeButtonContainer.getChildren().setAll(upgradeButton);
    }

    private void clearProfileHero() {
        profileRolePill.textProperty().unbind();
        profileNameLabel.setText("-");
        profileEmailLabel.setText("-");
        profileRolePill.setText(I18n.t("account.role"));
        profileAvatarInitial.setText("?");
        upgradeButtonContainer.getChildren().clear();
        showInitialAvatar();
    }

    private void applyAvatarImage(Path imagePath) {
        Image image = AvatarImageHelper.resolveAvatarImage(imagePath);
        if (image == null) {
            if (imagePath != null) {
                log.debug("Failed to render profile avatar from path '{}'", imagePath);
            }
            showInitialAvatar();
            return;
        }

        profileAvatarImage.setImage(image);
        profileAvatarImage.setManaged(true);
        profileAvatarImage.setVisible(true);
        profileAvatarInitial.setManaged(false);
        profileAvatarInitial.setVisible(false);
    }

    private void showInitialAvatar() {
        profileAvatarImage.setImage(null);
        profileAvatarImage.setManaged(false);
        profileAvatarImage.setVisible(false);
        profileAvatarInitial.setManaged(true);
        profileAvatarInitial.setVisible(true);
    }


    private void onLogOut() {
        authService.logout();
        toast.success("Logged off successfully");
        guardedNavigator.goTo(getRouter(), RouteIds.START);
        refreshProfileHero();
    }
}
