package com.triplify.ui.shared.component.badge.view;

import com.triplify.ui.shared.component.badge.viewmodel.BadgeViewModel;
import com.triplify.ui.shared.util.Localization;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class BadgeView extends VBox {
    static private final String LOCKED_CLASS = "badge-locked";
    private static final String DEFAULT_BADGE_RESOURCE = "/com/triplify/ui/shared/component/badge/images/default.png";
    private static final Image DEFAULT_BADGE_IMAGE = loadDefaultBadgeImage();
    private static final double TITLE_WIDTH = 120.0;
    private static final double TITLE_SINGLE_LINE_HEIGHT = 20.0;
    private static final double TITLE_DOUBLE_LINE_HEIGHT = 40.0;

    @FXML private ImageView badgeImage;
    @FXML private Label badgeName;
    @FXML private Label badgeStat;

    public BadgeView() {
        URL fxmlUrl = getClass().getResource("/com/triplify/ui/shared/component/badge/view/AppBadge.fxml");
        if (fxmlUrl == null) throw new IllegalStateException("AppBadge.fxml not found");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AppBadge.fxml", e);
        }

        badgeName.textProperty().addListener((obs, oldValue, newValue) -> updateTitleLayout());
        badgeName.widthProperty().addListener((obs, oldValue, newValue) -> updateTitleLayout());
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(this::updateTitleLayout);
            }
        });
        updateTitleLayout();
    }

    public void update(BadgeViewModel badge) {
        badgeName.textProperty().unbind();
        badgeStat.textProperty().unbind();
        Localization.bindLocalizedText(badgeName.textProperty(), badge);

        badgeStat.textProperty().bind(
                Localization.textBinding("badge.countriesVisited")
                        .concat(": ")
                        .concat(badge.getCurrentValue())
                        .concat("/")
                        .concat(badge.getRequiredValue())
        );
        badgeImage.setImage(resolveBadgeImage(badge.getImage()));

        if (badge.isUnlocked()) {
            getStyleClass().remove(LOCKED_CLASS);
        } else if (!getStyleClass().contains(LOCKED_CLASS)) {
            getStyleClass().add(LOCKED_CLASS);
        }
    }

    private void updateTitleLayout() {
        String title = badgeName.getText();
        boolean usesTwoLines = shouldUseTwoLines(title);
        badgeName.setMinHeight(TITLE_SINGLE_LINE_HEIGHT);
        badgeName.setPrefHeight(usesTwoLines ? TITLE_DOUBLE_LINE_HEIGHT : TITLE_SINGLE_LINE_HEIGHT);
        badgeName.setMaxHeight(TITLE_DOUBLE_LINE_HEIGHT);
    }

    private boolean shouldUseTwoLines(String title) {
        if (title == null || title.isBlank()) {
            return false;
        }

        badgeName.applyCss();

        double width = badgeName.getWidth();
        if (width <= 0) {
            width = badgeName.getPrefWidth() > 0 ? badgeName.getPrefWidth() : TITLE_WIDTH;
        }

        double previousPrefHeight = badgeName.getPrefHeight();
        badgeName.setPrefHeight(Region.USE_COMPUTED_SIZE);
        double requiredHeight = badgeName.prefHeight(width);
        badgeName.setPrefHeight(previousPrefHeight);

        return requiredHeight > TITLE_SINGLE_LINE_HEIGHT + 0.5;
    }

    private Image resolveBadgeImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            Image image = new Image(imageUrl, false);
            if (!image.isError()) {
                return image;
            }
        }
        return DEFAULT_BADGE_IMAGE;
    }

    private static Image loadDefaultBadgeImage() {
        URL defaultUrl = BadgeView.class.getResource(DEFAULT_BADGE_RESOURCE);
        if (defaultUrl == null) {
            throw new IllegalStateException("Default badge image not found at " + DEFAULT_BADGE_RESOURCE);
        }
        return new Image(defaultUrl.toExternalForm(), false);
    }
}
