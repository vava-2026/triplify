package com.triplify.ui.shared.component.badge.view;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.component.badge.model.Badge;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class BadgeView extends VBox {
    static private final String LOCKED_CLASS = "badge-locked";

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
    }

    public void update(Badge badge) {
        badgeName.setText(badge.getName());

        badgeStat.textProperty().bind(
                Bindings.createStringBinding(() -> I18n.t("badge.countriesVisited")
                        + ": "
                        + badge.getCurrentValue()
                        + "/"
                        + badge.getRequiredValue()
                        , I18n.bundleProperty())
        );

        // for placeholder generation (TODO: replace with a proper placeholder image)
        double imageSize = 75;

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

        if (badge.isUnlocked() && !getStyleClass().contains(LOCKED_CLASS)) {
            getStyleClass().add(LOCKED_CLASS);
        }
        else {
            getStyleClass().remove(LOCKED_CLASS);
        }
    }
}
