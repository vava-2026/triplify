package com.triplify.ui.shared.component.trip.view;

import com.triplify.application.response.TripResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class TripCardView implements Initializable {

    private static final double DETAILS_CARD_WIDTH = 257;

    private static final URL FXML_URL = TripCardView.class.getResource(
            "/com/triplify/ui/shared/component/trip/view/TripCard.fxml"
    );

    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();
    private static final int MAX_IMAGE_WIDTH = 600;
    private static final int MAX_IMAGE_HEIGHT = 400;

    private static final BackgroundSize COVER_SIZE = new BackgroundSize(
            1, 1, true, true, false, true
    );

    @FXML private VBox root;
    @FXML private StackPane media;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label dateLabel;

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
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        media.setClip(clip);

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

        media.getStyleClass().removeIf(style -> style.startsWith("trip-cover-"));

        boolean imageApplied = false;
        if (trip.coverUrl() != null && !trip.coverUrl().isBlank()) {
            Image image = resolveImage(trip.coverUrl());
            if (image != null && !image.isError()) {
                imageApplied = true;
                BackgroundImage bg = new BackgroundImage(
                        image,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        COVER_SIZE
                );
                media.setBackground(new Background(bg));
            }
        }

        if (!imageApplied) {
            media.setBackground(null);
            media.setStyle(null);
            if (trip.coverKey() != null && !trip.coverKey().isBlank()) {
                media.getStyleClass().add("trip-cover-" + trip.coverKey());
            } else {
                media.getStyleClass().add("trip-cover-default");
            }
        }

        statusLabel.setText(trip.status() == null ? "Unknown" : trip.status().getLabel());
        statusLabel.getStyleClass().removeIf(style -> style.startsWith("trip-status-"));
        if (trip.status() != null) {
            statusLabel.getStyleClass().add(trip.status().getCssClass());
        }
    }

    private Image resolveImage(String coverUrl) {
        return IMAGE_CACHE.computeIfAbsent(coverUrl, url -> {
            String resolved = url;
            if (url.startsWith("/")) {
                URL resource = getClass().getResource(url);
                if (resource == null) return null;
                resolved = resource.toExternalForm();
            }
            return new Image(resolved, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT, true, true);
        });
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

    public static TripCardView createForDetails(TripResponse trip, String dateRange, Runnable onOpen) {
        TripCardView view = create(trip, dateRange, onOpen);
        view.root.setMinWidth(DETAILS_CARD_WIDTH);
        view.root.setPrefWidth(DETAILS_CARD_WIDTH);
        view.root.setMaxWidth(DETAILS_CARD_WIDTH);
        return view;
    }
}
