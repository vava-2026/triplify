package com.triplify.ui.pages.trips.view;

import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.util.DisplayUtils;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.Localization;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TripCardView implements Initializable {
    private static final URL FXML_URL = TripCardView.class.getResource(
            "/com/triplify/ui/pages/trips/TripCard.fxml"
    );

    private static final int CATEGORY_EMOJI_SIZE = 18;

    @FXML private VBox root;
    @FXML private StackPane media;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;
    @FXML private HBox categoryRow;
    @FXML private ImageView categoryEmojiView;
    @FXML private Label categoryLabel;
    @FXML private Label dateLabel;

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
        EditorUtils.installRoundedClip(media, 12);

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

    public void setOnOpen(Runnable onOpen) {
        this.onOpen = onOpen;
    }

    public void setTrip(TripResponse trip, String dateRange) {
        if (trip == null) return;
        titleLabel.setText(trip.title());
        DisplayUtils.bindEmoji(categoryRow, categoryLabel, categoryEmojiView, trip.category(), trip.category().emojiUnicode(), CATEGORY_EMOJI_SIZE);
        dateLabel.setText(dateRange);

        String coverUrl = DisplayUtils.deriveCoverUrl(trip.coverImage());
        Image image = EditorUtils.resolveCoverImage(coverUrl);
        EditorUtils.applyCoverBackground(media, image);

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
        return create(trip, dateRange, onOpen);
    }
}
