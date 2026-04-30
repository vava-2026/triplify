package com.triplify.ui.pages.places.view;

import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.ui.shared.util.DisplayUtils;
import com.triplify.ui.shared.util.EditorUtils;
import com.triplify.ui.shared.util.Localization;
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

public class PlaceCardView implements Initializable {
    private static final int COUNTRY_EMOJI_SIZE = 18;
    private static final URL FXML_URL = PlaceCardView.class.getResource(
            "/com/triplify/ui/pages/places/PlaceCard.fxml"
    );

    @FXML private VBox root;
    @FXML private StackPane media;
    @FXML private Label titleLabel;
    @FXML private HBox countryRow;
    @FXML private ImageView countryEmojiView;
    @FXML private Label countryLabel;
    @FXML private Label coordLabel;

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

        String coverUrl = DisplayUtils.deriveCoverUrl(place.coverImage());
        EditorUtils.applyCoverBackground(media, EditorUtils.resolveCoverImage(coverUrl));

        titleLabel.setText(EditorUtils.safeText(place.title(), "Untitled place"));
        bindCountry(place);
        coordLabel.setText(String.format(java.util.Locale.US, "%.4f, %.4f", place.latitude(), place.longitude()));
    }

    public static PlaceCardView create(PlaceResponse place, Runnable onOpen) {
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

    private void bindCountry(PlaceResponse place) {
        countryLabel.textProperty().unbind();
        Localization.bindLocalizedText(countryLabel.textProperty(), place.country());

        boolean hasCountry = place.country() != null;
        countryRow.setVisible(hasCountry);
        countryRow.setManaged(hasCountry);

        if (!hasCountry) {
            countryEmojiView.setVisible(false);
            countryEmojiView.setManaged(false);
            countryEmojiView.setImage(null);
            return;
        }

        EditorUtils.applyEmojiImage(countryEmojiView, place.country().emojiUnicode(), COUNTRY_EMOJI_SIZE);
    }
}
