package com.triplify.ui.pages.routes.view;

import com.triplify.application.usecase.triproute.dto.TripRouteResponse;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.util.DisplayUtils;
import com.triplify.ui.shared.util.EditorUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class TripRouteCardView implements Initializable {

    private static final URL FXML_URL = TripRouteCardView.class.getResource(
            "/com/triplify/ui/pages/routes/TripRouteCard.fxml"
    );

    @FXML private VBox root;
    @FXML private StackPane media;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;
    @FXML private Label placesLabel;
    @FXML private Label distanceLabel;

    private StatusEnum currentStatus = StatusEnum.PLANNED;
    private TripRouteResponse currentTripRoute;
    private Runnable onOpen;

    private final ChangeListener<ResourceBundle> i18nBundleListener = (obs, oldBundle, newBundle) -> {
        DisplayUtils.applyStatus(statusLabel, currentStatus);
        applyLocalizedMeta();
    };
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
        root.setCursor(onOpen == null ? javafx.scene.Cursor.DEFAULT : javafx.scene.Cursor.HAND);
    }

    public void setTripRoute(TripRouteResponse tripRoute) {
        if (tripRoute == null || tripRoute.route() == null) return;
        this.currentTripRoute = tripRoute;

        String coverUrl = DisplayUtils.deriveCoverUrl(tripRoute.route().coverImage());
        EditorUtils.applyCoverBackgroundAsync(media, coverUrl);

        titleLabel.setText(tripRoute.route().title());
        currentStatus = tripRoute.status();
        DisplayUtils.applyStatus(statusLabel, tripRoute.status());
        applyLocalizedMeta();
    }

    private void applyLocalizedMeta() {
        if (currentTripRoute == null || currentTripRoute.route() == null) return;
        int count = currentTripRoute.route().places() == null ? 0 : currentTripRoute.route().places().size();
        placesLabel.setText(formatPlacesCount(count));
        distanceLabel.setText(formatDistance(currentTripRoute.route().length()));
    }

    private String formatPlacesCount(int count) {
        String key = count == 1 ? "place.details.route.places.one" : "place.details.route.places.other";
        return String.format(Locale.US, I18n.t(key), count);
    }

    private String formatDistance(double distanceKm) {
        return String.format(Locale.US, I18n.t("place.details.route.distance"), distanceKm);
    }

    public static TripRouteCardView create(TripRouteResponse tripRoute, Runnable onOpen) {
        if (FXML_URL == null) {
            throw new IllegalStateException("TripRouteCard.fxml not found");
        }
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load TripRouteCard.fxml", e);
        }
        TripRouteCardView view = loader.getController();
        view.setOnOpen(onOpen);
        view.setTripRoute(tripRoute);
        return view;
    }
}
