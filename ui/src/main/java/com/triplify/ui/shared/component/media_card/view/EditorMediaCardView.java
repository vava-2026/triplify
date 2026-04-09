package com.triplify.ui.shared.component.media_card.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class EditorMediaCardView extends VBox {

    private static final double DEFAULT_PREVIEW_WIDTH = 152;
    private static final double DEFAULT_PREVIEW_HEIGHT = 96;
    private static final double DEFAULT_CLIP_RADIUS = 16;

    private static final URL FXML_URL = EditorMediaCardView.class.getResource(
            "/com/triplify/ui/shared/component/media_card/view/EditorMediaCard.fxml"
    );
    private static final URL CSS_URL = EditorMediaCardView.class.getResource(
            "/com/triplify/ui/shared/component/editor/css/editor_components.css"
    );

    @FXML private ImageView previewImageView;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Button removeButton;

    private final Rectangle previewClip = new Rectangle();

    public EditorMediaCardView() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load EditorMediaCard.fxml", e);
        }

        if (CSS_URL != null) {
            getStylesheets().add(CSS_URL.toExternalForm());
        }

        previewClip.setArcWidth(DEFAULT_CLIP_RADIUS);
        previewClip.setArcHeight(DEFAULT_CLIP_RADIUS);
        previewClip.widthProperty().bind(previewImageView.fitWidthProperty());
        previewClip.heightProperty().bind(previewImageView.fitHeightProperty());
        previewImageView.setClip(previewClip);
        setPreviewSize(DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    public void setPreviewImage(Image image) {
        previewImageView.setImage(image);
    }

    public void setCardWidth(double width) {
        setPrefWidth(width);
        setMinWidth(width);
        setMaxWidth(width);
    }

    public void setPreviewSize(double width, double height) {
        previewImageView.setFitWidth(width);
        previewImageView.setFitHeight(height);
    }

    public void setOnRemove(Runnable action) {
        removeButton.setOnAction(event -> action.run());
    }

    public void setRemoveVisible(boolean visible) {
        removeButton.setVisible(visible);
        removeButton.setManaged(visible);
    }
}
