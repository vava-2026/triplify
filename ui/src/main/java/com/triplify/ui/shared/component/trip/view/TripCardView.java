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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TripCardView implements Initializable {

    private static final URL FXML_URL = TripCardView.class.getResource(
            "/com/triplify/ui/shared/component/trip/view/TripCard.fxml"
    );

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
        root.setMinWidth(240);
        root.setPrefWidth(240);
        root.setMaxWidth(240);

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

        if (trip.coverUrl() != null && !trip.coverUrl().isBlank()) {
            Image image = new Image(trip.coverUrl(), true);
            image.errorProperty().addListener((obs, oldV, newV) -> {
                if (newV) {
                    coverImage.setImage(null);
                    coverImage.setVisible(false);
                }
            });
            coverImage.setImage(image);
            coverImage.setVisible(true);
        } else {
            coverImage.setImage(null);
            coverImage.setVisible(false);
        }

        statusLabel.setText(trip.status() == null ? "Unknown" : trip.status().getLabel());
        statusLabel.getStyleClass().removeIf(style -> style.startsWith("trip-status-"));
        if (trip.status() != null) {
            statusLabel.getStyleClass().add(trip.status().getCssClass());
        }

        media.getStyleClass().removeIf(style -> style.startsWith("trip-cover-"));
        if (trip.coverKey() != null && !trip.coverKey().isBlank()) {
            media.getStyleClass().add("trip-cover-" + trip.coverKey());
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
