package com.triplify.ui.shared.component.upload_panel.view;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class ImageUploadPanelView extends VBox {

    private static final URL FXML_URL = ImageUploadPanelView.class.getResource(
            "/com/triplify/ui/shared/component/upload_panel/view/ImageUploadPanel.fxml"
    );
    private static final URL CSS_URL = ImageUploadPanelView.class.getResource(
            "/com/triplify/ui/shared/component/editor/css/editor_components.css"
    );

    @FXML private FontIcon sectionIconNode;
    @FXML private Label sectionTitleLabel;
    @FXML private StackPane uploadArea;
    @FXML private ImageView coverPreview;
    @FXML private VBox uploadPlaceholder;
    @FXML private Label uploadTitleLabel;
    @FXML private Label uploadSubtitleLabel;
    @FXML private Label selectedImageLabel;

    public ImageUploadPanelView() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ImageUploadPanel.fxml", e);
        }

        if (CSS_URL != null) {
            getStylesheets().add(CSS_URL.toExternalForm());
        }
    }

    public StringProperty sectionTitleProperty() {
        return sectionTitleLabel.textProperty();
    }

    public String getSectionTitle() {
        return sectionTitleLabel.getText();
    }

    public void setSectionTitle(String title) {
        sectionTitleLabel.setText(title);
    }

    public String getSectionIconLiteral() {
        return sectionIconNode.getIconLiteral();
    }

    public void setSectionIconLiteral(String iconLiteral) {
        sectionIconNode.setIconLiteral(iconLiteral);
    }

    public StringProperty uploadTitleProperty() {
        return uploadTitleLabel.textProperty();
    }

    public String getUploadTitle() {
        return uploadTitleLabel.getText();
    }

    public void setUploadTitle(String title) {
        uploadTitleLabel.setText(title);
    }

    public StringProperty uploadSubtitleProperty() {
        return uploadSubtitleLabel.textProperty();
    }

    public String getUploadSubtitle() {
        return uploadSubtitleLabel.getText();
    }

    public void setUploadSubtitle(String subtitle) {
        uploadSubtitleLabel.setText(subtitle);
    }

    public StackPane getUploadArea() {
        return uploadArea;
    }

    public ImageView getCoverPreview() {
        return coverPreview;
    }

    public VBox getUploadPlaceholder() {
        return uploadPlaceholder;
    }

    public Label getSelectedImageLabel() {
        return selectedImageLabel;
    }
}
