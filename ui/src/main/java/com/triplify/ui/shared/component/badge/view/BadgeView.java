package com.triplify.ui.shared.component.badge.view;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.badge.model.Badge;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.binding.Bindings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class BadgeView extends VBox {
    static private final String LOCKED_CLASS = "badge-locked";
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

    public void update(Badge badge) {
        badgeName.textProperty().unbind();
        badgeStat.textProperty().unbind();
        Localization.bindLocalizedText(badgeName.textProperty(), badge);

        badgeStat.textProperty().bind(
                Bindings.createStringBinding(() -> I18n.t("badge.countriesVisited")
                        + ": "
                        + badge.getCurrentValue()
                        + "/"
                        + badge.getRequiredValue()
                        , I18n.bundleProperty())
        );

        // for placeholder generation (TODO: replace with a proper placeholder image)
        double imageSize = 100;

        if (badge.getImage() != null && !badge.getImage().isBlank()) {
            try {
                badgeImage.setImage(new Image(badge.getImage(), true));
            } catch (Exception ignored) {}
        }
        else {
            // Generate a red circle placeholder image (TODO: replace with a proper placeholder)
            Canvas canvas = new Canvas(imageSize, imageSize);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(javafx.scene.paint.Color.RED);
            gc.fillOval(0, 0, imageSize, imageSize);
            WritableImage placeholderImage = new javafx.scene.image.WritableImage((int)imageSize, (int)imageSize);
            canvas.snapshot(null, placeholderImage);
            badgeImage.setImage(placeholderImage);
        }

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
}
