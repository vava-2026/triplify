package com.triplify.ui.shared.component.upload_panel.view;

import com.triplify.ui.shared.util.EditorUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.function.Consumer;

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
    @FXML private FontIcon uploadIconNode;
    @FXML private Label uploadTitleLabel;
    @FXML private Label uploadSubtitleLabel;
    @FXML private Label selectedImageLabel;
    @FXML private VBox selectedMetaBox;
    @FXML private Label selectedStatusLabel;
    @FXML private Label selectedExpiryLabel;

    private final StringProperty sectionIconLiteral = new SimpleStringProperty(this, "sectionIconLiteral", "fth-image");
    private final StringProperty uploadIconLiteral = new SimpleStringProperty(this, "uploadIconLiteral", "fth-image");
    private final IntegerProperty sectionIconSize = new SimpleIntegerProperty(this, "sectionIconSize", 14);
    private final IntegerProperty uploadIconSize = new SimpleIntegerProperty(this, "uploadIconSize", 30);
    private final DoubleProperty panelWidth = new SimpleDoubleProperty(this, "panelWidth", Double.NaN);
    private final DoubleProperty panelHeight = new SimpleDoubleProperty(this, "panelHeight", Double.NaN);
    private Consumer<File> onImageFileSelected;
    private Consumer<File> onUnsupportedImageFile;

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

        sectionIconLiteral.set(sectionIconNode.getIconLiteral());
        uploadIconLiteral.set(uploadIconNode.getIconLiteral());

        sectionIconLiteral.addListener((obs, oldVal, newVal) -> sectionIconNode.setIconLiteral(newVal));
        uploadIconLiteral.addListener((obs, oldVal, newVal) -> uploadIconNode.setIconLiteral(newVal));
        sectionIconSize.addListener((obs, oldVal, newVal) -> sectionIconNode.setIconSize(newVal.intValue()));
        uploadIconSize.addListener((obs, oldVal, newVal) -> uploadIconNode.setIconSize(newVal.intValue()));
        panelWidth.addListener((obs, oldVal, newVal) -> applyPanelSize());
        panelHeight.addListener((obs, oldVal, newVal) -> applyPanelSize());
    }

    public StringProperty sectionTitleProperty() {
        return sectionTitleLabel.textProperty();
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

    public StringProperty sectionIconLiteralProperty() {
        return sectionIconLiteral;
    }

    public String getSectionTitle() {
        return sectionTitleLabel.getText();
    }

    public void setSectionTitle(String title) {
        sectionTitleLabel.setText(title);
    }

    public String getSectionIconLiteral() {
        return sectionIconLiteral.get();
    }

    public void setSectionIconLiteral(String iconLiteral) {
        sectionIconLiteral.set(iconLiteral);
    }

    public void setSectionIconSize(int size) {
        sectionIconSize.set(size);
    }

    public StringProperty uploadIconLiteralProperty() {
        return uploadIconLiteral;
    }

    public String getUploadIconLiteral() {
        return uploadIconLiteral.get();
    }

    public void setUploadIconLiteral(String iconLiteral) {
        uploadIconLiteral.set(iconLiteral);
    }

    public void setUploadIconSize(int size) {
        uploadIconSize.set(size);
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

    public StringProperty selectedImageTextProperty() {
        return selectedImageLabel.textProperty();
    }

    public BooleanProperty selectedImageVisibleProperty() {
        return selectedImageLabel.visibleProperty();
    }

    public void setSelectedImageText(String text) {
        selectedImageLabel.setText(text);
    }

    public void showSelectedImageText(boolean visible) {
        selectedImageLabel.setVisible(visible);
        selectedImageLabel.setManaged(visible);
    }

    public void setSelectedStatusText(String text) {
        selectedStatusLabel.setText(text);
    }

    public void setSelectedExpiryText(String text) {
        selectedExpiryLabel.setText(text);
    }

    public void showSelectedExpiry(boolean visible) {
        selectedExpiryLabel.setVisible(visible);
        selectedExpiryLabel.setManaged(visible);
    }

    public void showSelectedMeta(boolean visible) {
        selectedMetaBox.setVisible(visible);
        selectedMetaBox.setManaged(visible);
    }

    public void setPreviewVisible(boolean visible) {
        coverPreview.setVisible(visible);
        coverPreview.setManaged(visible);
    }

    public void setPlaceholderVisible(boolean visible) {
        uploadPlaceholder.setVisible(visible);
        uploadPlaceholder.setManaged(visible);
    }

    public void setPanelSize(double width, double height) {
        panelWidth.set(width);
        panelHeight.set(height);
    }

    public void setOnUploadClicked(EventHandler<? super MouseEvent> handler) {
        uploadArea.setOnMouseClicked(handler);
    }

    public void setOnImageFileSelected(Consumer<File> onImageFileSelected) {
        this.onImageFileSelected = onImageFileSelected;
    }

    public void setOnUnsupportedImageFile(Consumer<File> onUnsupportedImageFile) {
        this.onUnsupportedImageFile = onUnsupportedImageFile;
    }

    public void installDefaultImageDragAndDrop() {
        uploadArea.setOnDragOver(this::handleDefaultDragOver);
        uploadArea.setOnDragExited(this::handleDefaultDragExited);
        uploadArea.setOnDragDropped(this::handleDefaultDragDropped);
    }

    public void setOnUploadDragOver(EventHandler<? super DragEvent> handler) {
        uploadArea.setOnDragOver(handler);
    }

    public void setOnUploadDragExited(EventHandler<? super DragEvent> handler) {
        uploadArea.setOnDragExited(handler);
    }

    public void setOnUploadDragDropped(EventHandler<? super DragEvent> handler) {
        uploadArea.setOnDragDropped(handler);
    }

    private void applyPanelSize() {
        double width = panelWidth.get();
        double height = panelHeight.get();

        if (!Double.isNaN(width)) {
            setMinWidth(width);
            setPrefWidth(width);
            setMaxWidth(width);
        }
        if (!Double.isNaN(height)) {
            setMinHeight(height);
            setPrefHeight(height);
            setMaxHeight(height);
        }
    }

    private void handleDefaultDragOver(DragEvent event) {
        File file = extractFirstDraggedFile(event);
        if (file != null && EditorUtils.isSupportedImageFile(file)) {
            event.acceptTransferModes(TransferMode.COPY);
            toggleUploadAreaActiveState(true);
        }
        event.consume();
    }

    private void handleDefaultDragExited(DragEvent event) {
        toggleUploadAreaActiveState(false);
        event.consume();
    }

    private void handleDefaultDragDropped(DragEvent event) {
        File file = extractFirstDraggedFile(event);
        boolean completed = false;
        if (file != null) {
            if (EditorUtils.isSupportedImageFile(file)) {
                if (onImageFileSelected != null) {
                    onImageFileSelected.accept(file);
                }
                completed = true;
            } else if (onUnsupportedImageFile != null) {
                onUnsupportedImageFile.accept(file);
            }
        }

        toggleUploadAreaActiveState(false);
        event.setDropCompleted(completed);
        event.consume();
    }

    private File extractFirstDraggedFile(DragEvent event) {
        if (event.getDragboard() == null || !event.getDragboard().hasFiles() || event.getDragboard().getFiles().isEmpty()) {
            return null;
        }
        return event.getDragboard().getFiles().getFirst();
    }

    private void toggleUploadAreaActiveState(boolean active) {
        if (active) {
            if (!uploadArea.getStyleClass().contains("editor-upload-area-active")) {
                uploadArea.getStyleClass().add("editor-upload-area-active");
            }
            return;
        }
        uploadArea.getStyleClass().remove("editor-upload-area-active");
    }
}
