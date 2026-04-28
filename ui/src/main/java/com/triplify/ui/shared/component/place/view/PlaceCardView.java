package com.triplify.ui.shared.component.place.view;

import com.triplify.application.usecase.place.dto.PlaceResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceCardView implements Initializable {

    private static final double DETAILS_CARD_WIDTH = 257;
    private static final double DETAILS_CARD_HEIGHT = 353;
    private static final double DETAILS_IMAGE_HEIGHT = 184;
    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";
    private static final URL FXML_URL = PlaceCardView.class.getResource(
            "/com/triplify/ui/shared/component/place/view/PlaceCard.fxml"
    );
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    @FXML private VBox root;
    @FXML private ImageView coverImageView;
    @FXML private Label titleLabel;
    @FXML private Label countryLabel;
    @FXML private FontIcon coordIcon;
    @FXML private Label coordLabel;

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

    public void setPlace(PlaceResponse place) {
        if (place == null) return;
        coverImageView.setImage(loadImage(place));
        titleLabel.setText(safeText(place.title(), "Untitled place"));
        countryLabel.setText(place.country() == null ? "" : safeText(place.country().name(), ""));
        coordIcon.setIconLiteral("fth-map-pin");
        coordLabel.setText(String.format(java.util.Locale.US, "%.4f, %.4f", place.latitude(), place.longitude()));
    }

    public static PlaceCardView createForDetails(PlaceResponse place, Runnable onOpen) {
        if (FXML_URL == null) {
            throw new IllegalStateException("PlaceCard.fxml not found");
        }
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PlaceCard.fxml", e);
        }
        PlaceCardView view = loader.getController();
        view.setOnOpen(onOpen);
        view.setPlace(place);
        return view;
    }

    private Image loadImage(PlaceResponse place) {
        String imagePath = place.coverImage() != null && place.coverImage().url() != null
                ? place.coverImage().url().toString()
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
}
