package com.triplify.ui.shared.component.trip.view;

import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.ui.i18n.I18n;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
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
import lombok.Setter;

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

    @Setter
    private Runnable onOpen;
    private StatusEnum currentStatus;
    private final ChangeListener<ResourceBundle> i18nBundleListener = (obs, oldBundle, newBundle) -> applyStatus(currentStatus);
    private final WeakChangeListener<ResourceBundle> weakI18nBundleListener = new WeakChangeListener<>(i18nBundleListener);

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

        I18n.bundleProperty().addListener(weakI18nBundleListener);
    }

    public Node getRoot() {
        return root;
    }

    public void setTrip(TripResponse trip, String dateRange) {
        if (trip == null) return;
        titleLabel.setText(trip.title());
        categoryLabel.setText(trip.category() == null ? "" : trip.category().name());
        dateLabel.setText(dateRange);

        media.getStyleClass().removeIf(style -> style.startsWith("trip-cover-"));

        String coverUrl = resolveCoverUrl(trip);
        boolean imageApplied = false;
        if (coverUrl != null && !coverUrl.isBlank()) {
            Image image = resolveImage(coverUrl);
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
            media.getStyleClass().add("trip-cover-default");
        }

        currentStatus = trip.status();
        applyStatus(currentStatus);
    }

    private void applyStatus(StatusEnum status) {
        statusLabel.setText(resolveStatusLabel(status));
        statusLabel.getStyleClass().removeIf(style -> style.startsWith("trip-status-"));
        String statusClass = resolveStatusCssClass(status);
        if (statusClass != null) {
            statusLabel.getStyleClass().add(statusClass);
        }
    }

    private String resolveCoverUrl(TripResponse trip) {
        if (trip.coverImage() == null || trip.coverImage().url() == null) {
            return null;
        }
        return trip.coverImage().url().toUri().toString();
    }

    private String resolveStatusLabel(StatusEnum status) {
        if (status == null) {
            return I18n.t("trip.status.unknown");
        }

        return switch (status) {
            case VISITED -> I18n.t("trip.status.visited");
            case ONGOING -> I18n.t("trip.status.ongoing");
            case PLANNED, CANCELED -> I18n.t("trip.status.planned");
        };
    }

    private String resolveStatusCssClass(StatusEnum status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case VISITED -> "trip-status-visited";
            case ONGOING -> "trip-status-ongoing";
            case PLANNED, CANCELED -> "trip-status-planned";
        };
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
