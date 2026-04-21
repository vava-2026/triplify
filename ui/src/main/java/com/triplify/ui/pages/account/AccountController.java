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
import com.triplify.application.usecase.user.UserService;
import com.triplify.application.usecase.user.dto.UpdateUserAvatarRequest;
import com.triplify.application.usecase.user.dto.UpdateUserProfileRequest;
import com.triplify.application.usecase.user.dto.UserResponse;
import com.triplify.domain.error.ValidationError;
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
import com.triplify.ui.shared.component.license.view.LicenseModalView;
import com.triplify.ui.shared.component.input_item.InputItem;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class AccountController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private static final int BADGE_COLUMNS = 5;
    private static final double PROFILE_AVATAR_SIZE = 150;

    @FXML private GridPane badgesGrid;
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileRolePill;
    @FXML private Label profileAvatarInitial;
    @FXML private ImageView profileAvatarImage;
    @FXML private HBox profileNameDisplayRow;
    @FXML private StackPane upgradeButtonContainer;
    @FXML private StackPane logoutButtonContainer;
    @FXML private Button profileAvatarEditBtn;
    @FXML private Button profileEditNameBtn;
    @FXML private HBox profileNameEditorRow;
    @FXML private StackPane profileNameInputContainer;
    @FXML private StackPane profileNameSaveButtonContainer;

    @Inject private ToastService toast;
    @Inject private AuthService authService;
    @Inject private BadgeService badgeService;
    @Inject private ImageService imageService;
    @Inject private UserService userService;
    @Inject private UserSessionContext userSessionContext;
    @Inject private ErrorHandler errorHandler;
    @Inject private GuardedNavigator guardedNavigator;
    @Inject private PageAccessService pageAccessService;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private InputItem profileNameInput;
    private LicenseModalView licenseModal;

    @FXML
    public void initialize() {
        licenseModal = new LicenseModalView(fxmlLoader);
        setupAvatarButton();
        setupLogoutButton();
        setupProfileNameEditor();
        refreshProfileHero();
        loadBadges();
    }

    private void setupAvatarButton() {
        if (profileAvatarEditBtn != null) {
            profileAvatarEditBtn.setOnAction(event -> onChangeAvatar());
            profileAvatarEditBtn.setFocusTraversable(false);
        }
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

    private void setupProfileNameEditor() {
        profileNameInput = new InputItem("input.placeholder.username");
        profileNameInput.getStyleClass().add("profile-name-input");
        profileNameInputContainer.getChildren().setAll(profileNameInput);

        Button saveNameButton = AppButtonView.builder(fxmlLoader)
                .variant(ButtonVariant.PRIMARY)
                .labelBinding(Bindings.createStringBinding(() -> I18n.t("account.profile.save"), I18n.bundleProperty()))
                .icon("fth-check")
                .onAction(this::onSaveProfileName)
                .build();
        saveNameButton.setFocusTraversable(false);
        saveNameButton.getStyleClass().add("profile-name-save-btn");
        profileNameSaveButtonContainer.getChildren().setAll(saveNameButton);

        if (profileEditNameBtn != null) {
            profileEditNameBtn.setOnAction(ignored -> onEditName());
            profileEditNameBtn.setFocusTraversable(false);
        }
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
        if (profileNameInput != null) {
            profileNameInput.setText(user.username());
            profileNameInput.clearError();
        }
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
        upgradeButton.setOnAction(evt -> {
            if (licenseModal != null && upgradeButton.getScene() != null) {
                licenseModal.show(upgradeButton.getScene().getWindow());
            }
        });
        upgradeButtonContainer.getChildren().setAll(upgradeButton);
    }

    private void clearProfileHero() {
        profileRolePill.textProperty().unbind();
        profileNameLabel.setText("-");
        profileEmailLabel.setText("-");
        profileRolePill.setText(I18n.t("account.role"));
        profileAvatarInitial.setText("?");
        hideNameEditor();
        upgradeButtonContainer.getChildren().clear();
        showInitialAvatar();
    }

    private void onEditName() {
        if (!userSessionContext.isLoggedIn() || profileNameInput == null) {
            return;
        }

        SessionUser currentUser = userSessionContext.getCurrent().orElseThrow();
        profileNameInput.setText(currentUser.username());
        profileNameInput.clearError();

        if (profileNameDisplayRow != null) {
            profileNameDisplayRow.setManaged(false);
            profileNameDisplayRow.setVisible(false);
        }
        profileNameEditorRow.setManaged(true);
        profileNameEditorRow.setVisible(true);
    }

    private void hideNameEditor() {
        if (profileNameEditorRow == null) {
            return;
        }

        if (profileNameDisplayRow != null) {
            profileNameDisplayRow.setManaged(true);
            profileNameDisplayRow.setVisible(true);
        }
        profileNameEditorRow.setManaged(false);
        profileNameEditorRow.setVisible(false);

        if (profileNameInput != null) {
            profileNameInput.clearError();
        }
    }

    private void onSaveProfileName() {
        if (profileNameInput == null || !userSessionContext.isLoggedIn()) {
            return;
        }

        SessionUser currentUser = userSessionContext.getCurrent().orElseThrow();
        String username = profileNameInput.getText() == null ? "" : profileNameInput.getText().trim();
        profileNameInput.setText(username);
        profileNameInput.clearError();

        if (username.equals(currentUser.username())) {
            hideNameEditor();
            return;
        }

        Result<UserResponse> result = userService.updateUserProfile(new UpdateUserProfileRequest(username));
        result.onSuccess(this::onProfileUpdated);
        result.onFailure(error -> {
            if (error instanceof ValidationError validationError) {
                boolean usernameViolation = validationError.violations().stream()
                        .anyMatch(violation -> "username".equals(violation.field()));
                if (usernameViolation) {
                    profileNameInput.showErrorHighlightOnly();
                }
            }
            errorHandler.handle(error);
        });
    }

    private void onProfileUpdated(UserResponse userResponse) {
        SessionUser currentUser = userSessionContext.getCurrent().orElseThrow();
        SessionUser updatedUser = new SessionUser(
                currentUser.userId(),
                userResponse.username(),
                userResponse.email(),
                userResponse.role(),
                currentUser.avatarImageId()
        );
        userSessionContext.set(updatedUser);
        userSessionContext.save();

        toast.success(I18n.t("account.profile.saved"));
        hideNameEditor();
        refreshProfileHero();
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

        AvatarImageHelper.applyCoverSquare(profileAvatarImage, image, PROFILE_AVATAR_SIZE);
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

    private void onChangeAvatar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("account.avatar.dialog.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.t("account.avatar.dialog.filter"), "*.png", "*.jpg", "*.jpeg", "*.svg")
        );

        File file = chooser.showOpenDialog(profileAvatarEditBtn.getScene() == null ? null : profileAvatarEditBtn.getScene().getWindow());
        if (file != null) {
            handleAvatarChange(file);
        }
    }

    private void handleAvatarChange(File file) {
        if (!isSupportedImageFile(file)) {
            toast.warning(I18n.t("account.avatar.toast.unsupported"));
            return;
        }

        Result<UserResponse> result = userService.updateUserAvatar(new UpdateUserAvatarRequest(file.toPath()));
        result.onSuccess(userResponse -> {
            SessionUser currentUser = userSessionContext.getCurrent().orElseThrow();

            UUID avatarId = null;
            if (userResponse.avatar() != null && userResponse.avatar().id() != null && !userResponse.avatar().id().isBlank()) {
                try {
                    avatarId = UUID.fromString(userResponse.avatar().id());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid avatar ID in response: {}", userResponse.avatar().id(), e);
                }
            }

            SessionUser updatedUser = new SessionUser(
                    currentUser.userId(),
                    currentUser.username(),
                    currentUser.email(),
                    currentUser.role(),
                    avatarId
            );
            userSessionContext.set(updatedUser);
            userSessionContext.save();

            toast.success(I18n.t("account.avatar.toast.updated"));
            refreshProfileHero();
        });
        result.onFailure(error -> {
            log.warn("Failed to update avatar: {}", error.message());
            errorHandler.handle(error);
        });
    }

    private boolean isSupportedImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".svg");
    }

    private void onLogOut() {
        authService.logout();
        toast.success("Logged off successfully");
        guardedNavigator.goTo(getRouter(), RouteIds.START);
        refreshProfileHero();
    }
}
