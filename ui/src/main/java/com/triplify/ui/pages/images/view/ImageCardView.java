package com.triplify.ui.pages.images.view;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.ui.shared.util.EditorUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ImageCardView implements Initializable {

    private static final URL FXML_URL = ImageCardView.class.getResource(
            "/com/triplify/ui/pages/images/ImageCard.fxml"
    );
    private static final int DESCRIPTION_MAX_CHARS = 80;

    @FXML private VBox root;
    @FXML private StackPane media;
    @FXML private Label descriptionLabel;

    private Runnable onOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.setMaxWidth(Double.MAX_VALUE);
        media.setMaxWidth(Double.MAX_VALUE);
        EditorUtils.installRoundedClip(media, 10);
        root.setOnMouseClicked(e -> {
            if (onOpen != null) onOpen.run();
        });
    }

    public Node getRoot() {
        return root;
    }

    public void setImage(ImageResponse image) {
        if (image == null) return;

        if (image.url() != null) {
            Image cover = EditorUtils.resolveCoverImage(image.url().toString());
            EditorUtils.applyCoverBackground(media, cover);
        }

        String desc = image.description();
        if (desc != null && desc.length() > DESCRIPTION_MAX_CHARS) {
            desc = desc.substring(0, DESCRIPTION_MAX_CHARS) + "…";
        }
        descriptionLabel.setText(desc != null ? desc : "");
        descriptionLabel.setVisible(desc != null && !desc.isBlank());
        descriptionLabel.setManaged(desc != null && !desc.isBlank());
    }

    public static ImageCardView create(ImageResponse image, Runnable onOpen) {
        if (FXML_URL == null) throw new IllegalStateException("ImageCard.fxml not found");
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ImageCard.fxml", e);
        }
        ImageCardView view = loader.getController();
        view.onOpen = onOpen;
        view.setImage(image);
        return view;
    }
}
