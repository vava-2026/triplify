package com.triplify.ui.shared.component.route.view;

import com.triplify.application.usecase.route.dto.RouteResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class RouteCardView implements Initializable {

    private static final double DETAILS_CARD_WIDTH = 257;
    private static final double DETAILS_CARD_HEIGHT = 353;
    private static final double DETAILS_IMAGE_HEIGHT = 184;
    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final URL FXML_URL = RouteCardView.class.getResource(
            "/com/triplify/ui/shared/component/route/view/RouteCard.fxml"
    );
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    @FXML private VBox root;
    @FXML private ImageView coverImageView;
    @FXML private Label titleLabel;
    @FXML private Label placesLabel;
    @FXML private FontIcon distanceIcon;
    @FXML private Label distanceLabel;

    private Runnable onOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.setMinWidth(DETAILS_CARD_WIDTH);
        root.setPrefWidth(DETAILS_CARD_WIDTH);
        root.setMaxWidth(DETAILS_CARD_WIDTH);
        root.setMinHeight(DETAILS_CARD_HEIGHT);
        root.setPrefHeight(DETAILS_CARD_HEIGHT);
        root.setMaxHeight(DETAILS_CARD_HEIGHT);

        coverImageView.setFitWidth(DETAILS_CARD_WIDTH);
        coverImageView.setFitHeight(DETAILS_IMAGE_HEIGHT);
        coverImageView.setPreserveRatio(false);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(coverImageView.fitWidthProperty());
        clip.heightProperty().bind(coverImageView.fitHeightProperty());
        clip.setArcWidth(36);
        clip.setArcHeight(36);
        coverImageView.setClip(clip);

        root.setOnMouseClicked(event -> {
            if (onOpen != null) {
                onOpen.run();
            }
        });
    }

    public Node getRoot() {
        return root;
    }

    public void setOnOpen(Runnable onOpen) {
        this.onOpen = onOpen;
        root.setCursor(onOpen == null ? javafx.scene.Cursor.DEFAULT : javafx.scene.Cursor.HAND);
    }

    public void setRoute(RouteResponse route) {
        if (route == null) {
            return;
        }

        coverImageView.setImage(loadImage(route));
        titleLabel.setText(safeText(route.title(), "Untitled route"));
        placesLabel.setText(formatPlacesCount(route.places() == null ? 0 : route.places().size()));
        distanceIcon.setIconLiteral("fth-navigation");
        distanceLabel.setText(formatDistance(route.length()));
    }

    public static RouteCardView createForDetails(RouteResponse route, Runnable onOpen) {
        if (FXML_URL == null) {
            throw new IllegalStateException("RouteCard.fxml not found");
        }

        FXMLLoader loader = new FXMLLoader(FXML_URL);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load RouteCard.fxml", e);
        }

        RouteCardView view = loader.getController();
        view.setOnOpen(onOpen);
        view.setRoute(route);
        return view;
    }

    private Image loadImage(RouteResponse route) {
        String imagePath = route.coverImage() != null && route.coverImage().url() != null
                ? route.coverImage().url().toString()
                : DEFAULT_IMAGE;

        return IMAGE_CACHE.computeIfAbsent(imagePath, this::resolveImage);
    }

    private Image resolveImage(String imagePath) {
        String resolvedPath = imagePath == null || imagePath.isBlank() ? DEFAULT_IMAGE : imagePath;
        if (resolvedPath.startsWith("/")) {
            URL resource = getClass().getResource(resolvedPath);
            if (resource != null) {
                return new Image(resource.toExternalForm(), true);
            }
        }

        File file = new File(resolvedPath);
        if (file.exists()) {
            return new Image(file.toURI().toString(), true);
        }

        URL fallback = getClass().getResource(DEFAULT_IMAGE);
        return new Image(fallback.toExternalForm(), true);
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatPlacesCount(int count) {
        return count == 1 ? "1 place" : count + " places";
    }

    private String formatDistance(double distanceKm) {
        return String.format(java.util.Locale.US, "%.1f kilometers", distanceKm);
    }
}
