package com.triplify.ui.shared.component.trip.view;

import com.triplify.application.response.TripResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TripCardView implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(TripCardView.class);
    private static final URL FXML_URL = TripCardView.class.getResource(
            "/com/triplify/ui/shared/component/trip/view/TripCard.fxml"
    );
    private static final String LOCAL_COVER_BASE = "/com/triplify/ui/shared/images/trips/";
    private static final String LOCAL_COVER_BASE_ALT = "/com/triplify/ui/shared/Images/trips/";
    private static final double DEFAULT_CARD_WIDTH = 230;
    private static final double DEFAULT_MEDIA_HEIGHT = 184;
    private static final double TARGET_SCALE = 2;

    @FXML private VBox root;
    @FXML private StackPane media;
    @FXML private ImageView coverImage;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label dateLabel;

    private Runnable onOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        media.minWidthProperty().bind(root.widthProperty());
        media.prefWidthProperty().bind(root.widthProperty());
        media.maxWidthProperty().bind(root.widthProperty());

        media.minHeightProperty().set(Region.USE_PREF_SIZE);
        media.maxHeightProperty().set(Region.USE_PREF_SIZE);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(media.widthProperty());
        clip.heightProperty().bind(media.heightProperty());
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        media.setClip(clip);

        coverImage.fitWidthProperty().bind(media.widthProperty());
        coverImage.fitHeightProperty().bind(media.heightProperty());
        coverImage.setSmooth(true);

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
    }

    public void setTrip(TripResponse trip, String dateRange) {
        if (trip == null) return;
        titleLabel.setText(trip.name());
        categoryLabel.setText(trip.category());
        dateLabel.setText(dateRange);
        applyCover(trip.coverUrl(), trip.coverKey());

        statusLabel.setText(trip.status() == null ? "Unknown" : trip.status().getLabel());
        statusLabel.getStyleClass().removeIf(style -> style.startsWith("trip-status-"));
        if (trip.status() != null) {
            statusLabel.getStyleClass().add(trip.status().getCssClass());
        }
    }

    private void applyCover(String coverUrl, String coverKey) {
        coverImage.setImage(null);
        coverImage.setVisible(false);
        applyCoverFallback(coverKey);

        Image image = loadCoverImage(coverUrl);
        if (image == null || image.isError()) {
            return;
        }
        if (image.isBackgroundLoading()) {
            image.errorProperty().addListener((obs, oldV, newV) -> {
                if (newV) {
                    coverImage.setImage(null);
                    coverImage.setVisible(false);
                    applyCoverFallback(coverKey);
                }
            });
        }
        coverImage.setImage(image);
        coverImage.setVisible(true);
    }

    private Image loadCoverImage(String coverUrl) {
        if (coverUrl == null || coverUrl.isBlank()) {
            return null;
        }
        String trimmed = coverUrl.trim();
        log.info("Trip cover: resolving '{}'", trimmed);
        if (isRemoteOrFileUrl(trimmed)) {
            log.info("Trip cover: loading remote/file/data URL");
            return createImage(trimmed);
        }
        Image image = tryResourceImages(trimmed);
        if (image != null) {
            log.info("Trip cover: loaded from resources");
            return image;
        }
        File file = new File(trimmed);
        if (file.isFile()) {
            log.info("Trip cover: loading from file path '{}'", file.getAbsolutePath());
            return createImage(file.toURI().toString());
        }
        log.warn("Trip cover: not found for '{}'", trimmed);
        return null;
    }

    private Image tryResourceImages(String value) {
        if (value.startsWith("/")) {
            log.info("Trip cover: resource attempt '{}'", value);
            Image image = loadResourceImage(value);
            if (image != null) return image;
            String normalized = normalizeResourcePath(value);
            log.info("Trip cover: resource attempt '{}'", normalized);
            image = loadResourceImage(normalized);
            if (image != null) return image;
            return null;
        }
        log.info("Trip cover: resource attempt '{}'", LOCAL_COVER_BASE + value);
        Image image = loadResourceImage(LOCAL_COVER_BASE + value);
        if (image != null) return image;
        log.info("Trip cover: resource attempt '{}'", LOCAL_COVER_BASE_ALT + value);
        image = loadResourceImage(LOCAL_COVER_BASE_ALT + value);
        if (image != null) return image;
        log.info("Trip cover: resource attempt '/{}'", value);
        return loadResourceImage("/" + value);
    }

    private String normalizeResourcePath(String value) {
        String normalized = value.replace("\\", "/");
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    private Image loadResourceImage(String resourcePath) {
        try (InputStream stream = TripCardView.class.getResourceAsStream(resourcePath)) {
            if (stream == null) return null;
            return createImage(stream);
        } catch (IOException ignored) {
            return null;
        }
    }

    private Image createImage(String url) {
        double targetWidth = getTargetWidth();
        double targetHeight = getTargetHeight();
        return new Image(url, targetWidth, targetHeight, false, true, true);
    }

    private Image createImage(InputStream stream) {
        double targetWidth = getTargetWidth();
        double targetHeight = getTargetHeight();
        return new Image(stream, targetWidth, targetHeight, false, true);
    }

    private double getTargetWidth() {
        double width = root.getWidth();
        if (width <= 0) width = DEFAULT_CARD_WIDTH;
        return width * TARGET_SCALE;
    }

    private double getTargetHeight() {
        double height = media.getHeight();
        if (height <= 0) height = DEFAULT_MEDIA_HEIGHT;
        return height * TARGET_SCALE;
    }

    private boolean isRemoteOrFileUrl(String value) {
        String lower = value.toLowerCase();
        return lower.startsWith("http://")
                || lower.startsWith("https://")
                || lower.startsWith("file:")
                || lower.startsWith("data:");
    }

    private void applyCoverFallback(String coverKey) {
        media.getStyleClass().removeIf(style -> style.startsWith("trip-cover-"));
        if (coverKey != null && !coverKey.isBlank()) {
            media.getStyleClass().add("trip-cover-" + coverKey);
        } else {
            media.getStyleClass().add("trip-cover-default");
        }
    }

    public static TripCardView create(TripResponse trip, String dateRange, Runnable onOpen) {
        if (FXML_URL == null) throw new IllegalStateException("TripCard.fxml not found");
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load TripCard.fxml", e);
        }
        TripCardView view = loader.getController();
        view.setOnOpen(onOpen);
        view.setTrip(trip, dateRange);
        return view;
    }
}
