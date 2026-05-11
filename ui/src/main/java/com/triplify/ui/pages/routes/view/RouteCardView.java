package com.triplify.ui.pages.routes.view;

import com.triplify.application.usecase.route.dto.RouteResponse;
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
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class RouteCardView implements Initializable {

    private static final URL FXML_URL = RouteCardView.class.getResource(
            "/com/triplify/ui/pages/routes/RouteCard.fxml"
    );

    @FXML private VBox root;
    @FXML private StackPane media;
    @FXML private Label titleLabel;
    @FXML private Label placesLabel;
    @FXML private FontIcon distanceIcon;
    @FXML private Label distanceLabel;

    private Runnable onOpen;
    private RouteResponse currentRoute;
    private final ChangeListener<ResourceBundle> i18nBundleListener = (obs, oldBundle, newBundle) -> applyLocalizedMeta();
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

    public void setRoute(RouteResponse route) {
        if (route == null) {
            return;
        }
        this.currentRoute = route;
        titleLabel.setText(route.title());
        distanceIcon.setIconLiteral("fth-navigation");
        placesLabel.setText(formatPlacesCount(route.places() == null ? 0 : route.places().size()));
        distanceLabel.setText(formatDistance(route.length()));

        String coverUrl = DisplayUtils.deriveCoverUrl(route.coverImage());
        EditorUtils.applyCoverBackgroundAsync(media, coverUrl);
    }

    public static RouteCardView create(RouteResponse route, Runnable onOpen) {
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

    private String formatPlacesCount(int count) {
        String key = count == 1 ? "place.details.route.places.one" : "place.details.route.places.other";
        return String.format(Locale.US, I18n.t(key), count);
    }

    private String formatDistance(double distanceKm) {
        return String.format(Locale.US, I18n.t("place.details.route.distance"), distanceKm);
    }

    private void applyLocalizedMeta() {
        if (currentRoute == null) {
            placesLabel.setText("");
            distanceLabel.setText("");
            return;
        }
        placesLabel.setText(formatPlacesCount(currentRoute.places() == null ? 0 : currentRoute.places().size()));
        distanceLabel.setText(formatDistance(currentRoute.length()));
    }
}
