package com.triplify.ui.shared.component.place.view;

import com.triplify.application.usecase.place.dto.PlaceResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceCardView implements Initializable {

    private static final URL FXML_URL = PlaceCardView.class.getResource(
            "/com/triplify/ui/shared/component/place/view/TripPlaceCard.fxml"
    );

    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();
    private static final int MAX_IMAGE_WIDTH  = 600;
    private static final int MAX_IMAGE_HEIGHT = 400;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault());

    private static final BackgroundSize COVER_SIZE = new BackgroundSize(
            1, 1, true, true, false, true
    );

    @FXML private VBox      root;
    @FXML private StackPane media;
    @FXML private Label     statusLabel;
    @FXML private Label     titleLabel;
    @FXML private Label     countryLabel;
    @FXML private Label     dateLabel;

    private Runnable onOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.setMaxWidth(Double.MAX_VALUE);

        media.setMaxWidth(Double.MAX_VALUE);
        media.minHeightProperty().set(Region.USE_PREF_SIZE);
        media.maxHeightProperty().set(Region.USE_PREF_SIZE);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(media.widthProperty());
        clip.heightProperty().bind(media.heightProperty());
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        media.setClip(clip);

        StackPane.setMargin(statusLabel, new Insets(8, 8, 0, 0));

        root.setOnMouseClicked(e -> { if (onOpen != null) onOpen.run(); });
    }

    public Node     getRoot()              { return root; }
    public void     setOnOpen(Runnable r)  { this.onOpen = r; }

    public void setPlace(PlaceResponse place) {
        if (place == null) return;

        // title
        titleLabel.setText(place.title());

        // country — CountryResponse can be null if not loaded
        if (countryLabel != null) {
            String countryName = place.country() != null ? place.country().name() : "-";
            countryLabel.setText(countryName);
        }

        // date from createdAt Instant
        if (dateLabel != null) {
            dateLabel.setText(place.createdAt() != null
                    ? DATE_FORMAT.format(place.createdAt())
                    : "Date TBA");
        }

        // cover image from ImageResponse
        media.setBackground(null);
        media.getStyleClass().removeIf(s -> s.equals("trip-cover-default"));

        String imageUrl = place.coverImage() != null && place.coverImage().url() != null
                ? place.coverImage().url().toString()
                : null;
        boolean imageApplied = false;
        if (imageUrl != null && !imageUrl.isBlank()) {
            Image image = resolveImage(imageUrl);
            if (image != null && !image.isError()) {
                media.setBackground(new Background(new BackgroundImage(
                        image,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        COVER_SIZE
                )));
                imageApplied = true;
            }
        }
        if (!imageApplied) {
            media.getStyleClass().add("trip-cover-default");
        }

        // status badge — real PlaceResponse has no status, hide the label
        if (statusLabel != null) {
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
        }
    }

    private Image resolveImage(String imageUrl) {
        return IMAGE_CACHE.computeIfAbsent(imageUrl, url -> {
            String normalized = url.replace('\\', '/');
            String resolved = normalized;
            if (normalized.startsWith("/")) {
                URL resource = getClass().getResource(normalized);
                if (resource == null) return null;
                resolved = resource.toExternalForm();
            }
            return new Image(resolved, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT, true, true);
        });
    }

    public static PlaceCardView create(PlaceResponse place, Runnable onOpen) {
        if (FXML_URL == null) throw new IllegalStateException("TripPlaceCard.fxml not found");
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load TripPlaceCard.fxml", e);
        }
        PlaceCardView view = loader.getController();
        view.setOnOpen(onOpen);
        view.setPlace(place);
        return view;
    }
}