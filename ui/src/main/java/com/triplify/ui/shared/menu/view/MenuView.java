package com.triplify.ui.shared.menu.view;

import com.google.inject.Inject;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.GetImageByIdRequest;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.AppPage;
import com.triplify.ui.routing.PageAccessService;
import com.triplify.ui.shared.menu.viewmodel.MenuViewModel;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.FxmlLoadResult;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MenuView implements Initializable {

    public static final double SIDEBAR_WIDTH = 260;
    private static final double SIDEBAR_COLLAPSED_WIDTH = 0;

    @FXML private StackPane sidebarRoot;
    @FXML private VBox mainPageInner;
    @FXML private VBox navContainer;

    @FXML private Label accountRole;
    @FXML private Label accountNameLabel;
    @FXML private Label avatarLabel;
    @FXML private ImageView avatarImageView;

    private final MenuViewModel viewModel = new MenuViewModel();
    private final List<NavButtonView> navButtons = new ArrayList<>();

    private final FxmlLoaderHelper fxmlLoader;
    private final UserSessionContext userSessionContext;
    private final ImageService imageService;
    private final PageAccessService pageAccessService;
    private static final Logger log = LoggerFactory.getLogger(MenuView.class);
    private SidebarIslandView islandController;
    private Consumer<AppPage> navigationHandler;
    private String currentRoleLabelKey;

    @Inject
    public MenuView(
            FxmlLoaderHelper fxmlLoader,
            UserSessionContext userSessionContext,
            ImageService imageService,
            PageAccessService pageAccessService) {
        this.fxmlLoader = fxmlLoader;
        this.userSessionContext = userSessionContext;
        this.imageService = imageService;
        this.pageAccessService = pageAccessService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarRoot.setMaxHeight(Double.MAX_VALUE);
        mainPageInner.setMaxHeight(Double.MAX_VALUE);
        refreshAccountSection();
        renderNavigation();

        viewModel.activePrimaryPageProperty().addListener(
                (obs, oldVal, newVal) -> refreshActiveState(newVal));
        refreshActiveState(viewModel.getActivePrimaryPage());

        viewModel.collapsedProperty().addListener(
                (obs, oldVal, newVal) -> applyCollapsedState(newVal));
        applyCollapsedState(viewModel.isCollapsed());
    }

    public void setIslandController(SidebarIslandView island) {
        this.islandController = island;
        island.setOnToggle(viewModel::toggleCollapsed);
        applyCollapsedState(viewModel.isCollapsed());
    }

    public MenuViewModel getViewModel() { return viewModel; }

    public void setNavigationHandler(Consumer<AppPage> navigationHandler) {
        this.navigationHandler = navigationHandler;
    }

    public void refreshAccountSection() {
        initializeAccountSection();
        renderNavigation();
    }

    @FXML
    private void onAccountClicked(MouseEvent event) {
        if (navigationHandler != null) {
            navigationHandler.accept(AppPage.ACCOUNT);
        }
    }

    private void refreshActiveState(AppPage active) {
        navButtons.forEach(btn ->
                btn.setActive(btn.getPage() == active));
    }

    private void applyCollapsedState(boolean collapsed) {
        mainPageInner.setVisible(!collapsed);
        mainPageInner.setManaged(!collapsed);

        double width = collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_WIDTH;
        sidebarRoot.setPrefWidth(width);
        sidebarRoot.setMaxWidth(width);
        sidebarRoot.setMinWidth(width);

        if (islandController != null) {
            islandController.setCollapsed(collapsed, viewModel.isHideHeader());
        }
    }

    private void initializeAccountSection() {
        var currentUserOpt = userSessionContext.getCurrent();
        if (currentUserOpt.isEmpty()) {
            accountNameLabel.setText("");
            currentRoleLabelKey = null;
            accountRole.textProperty().unbind();
            accountRole.setText("");
            avatarLabel.setText("?");
            showInitialAvatar();
            avatarImageView.setClip(new Circle(19, 19, 19));
            return;
        }

        var currentUser = currentUserOpt.get();
        String username = currentUser.username();
        accountNameLabel.setText(username);
        currentRoleLabelKey = pageAccessService.getRoleLabelKey(currentUser.role());
        accountRole.textProperty().unbind();
        accountRole.textProperty().bind(Bindings.createStringBinding(
                () -> I18n.t(currentRoleLabelKey),
                I18n.bundleProperty()));
        avatarLabel.setText(extractInitial(username));
        showInitialAvatar();
        avatarImageView.setClip(new Circle(19, 19, 19));

        if (currentUser.avatarImageId() == null) {
            return;
        }

        var avatarResult = imageService.getImageById(new GetImageByIdRequest(currentUser.avatarImageId().toString()));
        avatarResult.onSuccess(image -> applyAvatarImage(image.url()));
        avatarResult.onFailure(error -> {
            log.debug("Avatar image not available for user '{}'", username);
            showInitialAvatar();
        });
    }

    private void applyAvatarImage(Path imagePath) {
        if (imagePath == null) {
            showInitialAvatar();
            return;
        }

        try {
            Image image = new Image(imagePath.toUri().toString(), true);
            if (image.isError()) {
                showInitialAvatar();
                return;
            }
            avatarImageView.setImage(image);
            avatarImageView.setManaged(true);
            avatarImageView.setVisible(true);
            avatarLabel.setManaged(false);
            avatarLabel.setVisible(false);
        } catch (RuntimeException ex) {
            log.debug("Failed to render avatar image from path '{}'", imagePath, ex);
            showInitialAvatar();
        }
    }

    private void showInitialAvatar() {
        avatarImageView.setImage(null);
        avatarImageView.setManaged(false);
        avatarImageView.setVisible(false);
        avatarLabel.setManaged(true);
        avatarLabel.setVisible(true);
    }

    private String extractInitial(String username) {
        if (username == null || username.isBlank()) {
            return "?";
        }
        return username.substring(0, 1).toUpperCase();
    }

    private void renderNavigation() {
        navContainer.getChildren().clear();
        navButtons.clear();

        for (AppPage page : pageAccessService.getPrimaryMenuPages(userSessionContext.getCurrent())) {
            FxmlLoadResult<?, NavButtonView> result = fxmlLoader.load("/com/triplify/ui/shared/menu/view/NavButton.fxml");
            NavButtonView button = result.controller().withPage(page);
            button.setOnSelect(() -> {
                if (navigationHandler != null) {
                    navigationHandler.accept(page);
                }
            });
            navContainer.getChildren().add(button.getButton());
            navButtons.add(button);
        }

        refreshActiveState(viewModel.getActivePrimaryPage());
    }
}
