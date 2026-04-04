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

        Rectangle clip = new Rectangle(152, 96);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        previewImageView.setClip(clip);
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

    public void setOnRemove(Runnable action) {
        removeButton.setOnAction(event -> action.run());
    }
}
