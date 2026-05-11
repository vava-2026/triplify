package com.triplify.ui.pages.places.view;

import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TripPlaceCardView implements Initializable {
    private static final int COUNTRY_EMOJI_SIZE = 18;
    private static final URL FXML_URL = PlaceCardView.class.getResource(
            "/com/triplify/ui/pages/places/TripPlaceCard.fxml"
    );

    @FXML
    private VBox root;
    @FXML private StackPane media;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;
    @FXML private HBox countryRow;
    @FXML private ImageView countryEmojiView;
    @FXML private Label countryLabel;
    @FXML private Label coordLabel;

    StatusEnum currentStatus = StatusEnum.PLANNED;

    private final ChangeListener<ResourceBundle> i18nBundleListener = (obs, oldBundle, newBundle) -> DisplayUtils.applyStatus(statusLabel, currentStatus);
    private final WeakChangeListener<ResourceBundle> weakI18nBundleListener = new WeakChangeListener<>(i18nBundleListener);

    private Runnable onOpen;

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

    public void setTripPlace(TripPlaceResponse tripPlace) {
        if (tripPlace == null) return;

        String coverUrl = DisplayUtils.deriveCoverUrl(tripPlace.place().coverImage());
        EditorUtils.applyCoverBackgroundAsync(media, coverUrl);

        titleLabel.setText(tripPlace.place().title());
        DisplayUtils.bindCountry(countryRow, countryLabel, countryEmojiView, tripPlace.place().country(), COUNTRY_EMOJI_SIZE);
        coordLabel.setText(String.format(java.util.Locale.US, "%.4f, %.4f", tripPlace.place().latitude(), tripPlace.place().longitude()));
        currentStatus= tripPlace.status();
        DisplayUtils.applyStatus(statusLabel, tripPlace.status());
    }

    public static TripPlaceCardView create(TripPlaceResponse tripPlace, Runnable onOpen) {
        if (FXML_URL == null) {
            throw new IllegalStateException("TripPlaceCard.fxml not found");
        }
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load TripPlaceCard.fxml", e);
        }
        TripPlaceCardView view = loader.getController();
        view.setOnOpen(onOpen);
        view.setTripPlace(tripPlace);
        return view;
    }
}
