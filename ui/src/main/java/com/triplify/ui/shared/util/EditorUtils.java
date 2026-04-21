package com.triplify.ui.shared.util;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.toast.ToastService;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;

public final class EditorUtils {

    private EditorUtils() {}

    public static String safeText(String value) {
        return value == null ? "" : value;
    }

    public static String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public static String normalizeKey(String value) {
        if (value == null || value.isBlank() || "0".equals(value.trim())) {
            return "";
        }
        return value.trim();
    }

    public static String formatMessage(String key, Object... args) {
        return MessageFormat.format(I18n.t(key), args);
    }

    public static boolean isSupportedImageFile(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    public static Image loadImage(String imagePath, String defaultImage, Class<?> resourceContext) {
        String resolved = imagePath == null || imagePath.isBlank() ? defaultImage : imagePath;
        if (resolved.startsWith("/")) {
            var resource = resourceContext.getResource(resolved);
            if (resource != null) {
                return new Image(resource.toExternalForm(), true);
            }
        }
        File file = new File(resolved);
        if (file.exists()) {
            return new Image(file.toURI().toString(), true);
        }
        var fallback = resourceContext.getResource(defaultImage);
        return new Image(fallback.toExternalForm(), true);
    }

    public static void configureButtonIcon(Button button, String iconLiteral, int size, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(size);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
    }

    public static void configureButtonIcon(Button button, String iconLiteral) {
        configureButtonIcon(button, iconLiteral, 14, "app-btn-icon");
    }

    public static void installRoundedClip(StackPane target, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(target.widthProperty());
        clip.heightProperty().bind(target.heightProperty());
        target.setClip(clip);
    }

    public static void toggleStyleClass(javafx.scene.Node node, String styleClass, boolean add) {
        if (add) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }

    public static void initializeCoverPreview(ImageView coverPreview, StackPane uploadArea) {
        coverPreview.setPreserveRatio(false);
        coverPreview.fitWidthProperty().bind(uploadArea.widthProperty());
        coverPreview.fitHeightProperty().bind(uploadArea.heightProperty());
        uploadArea.widthProperty().addListener((obs, o, n) -> updateCoverViewport(coverPreview, uploadArea));
        uploadArea.heightProperty().addListener((obs, o, n) -> updateCoverViewport(coverPreview, uploadArea));
        coverPreview.imageProperty().addListener((obs, o, n) -> updateCoverViewport(coverPreview, uploadArea));
    }

    public static void setCoverPreviewImage(ImageView coverPreview, StackPane uploadArea, Image image) {
        coverPreview.setImage(image);
        updateCoverViewport(coverPreview, uploadArea);
        if (image == null) {
            coverPreview.setViewport(null);
            return;
        }
        image.widthProperty().addListener((obs, o, n) -> updateCoverViewport(coverPreview, uploadArea));
        image.heightProperty().addListener((obs, o, n) -> updateCoverViewport(coverPreview, uploadArea));
        image.progressProperty().addListener((obs, o, n) -> updateCoverViewport(coverPreview, uploadArea));
    }

    public static void updateCoverViewport(ImageView coverPreview, StackPane uploadArea) {
        Image image = coverPreview.getImage();
        if (image == null) {
            coverPreview.setViewport(null);
            return;
        }
        double iw = image.getWidth(), ih = image.getHeight();
        double vw = uploadArea.getWidth(), vh = uploadArea.getHeight();
        if (iw <= 0 || ih <= 0 || vw <= 0 || vh <= 0) return;

        double ir = iw / ih, vr = vw / vh;
        if (ir > vr) {
            double cw = ih * vr;
            coverPreview.setViewport(new Rectangle2D((iw - cw) / 2, 0, cw, ih));
        } else {
            double ch = iw / vr;
            coverPreview.setViewport(new Rectangle2D(0, (ih - ch) / 2, iw, ch));
        }
    }

    public static void handleCoverImageFile(
            File file, ImageView coverPreview, StackPane uploadArea,
            VBox uploadPlaceholder, Label selectedImageLabel,
            ToastService toast, String previewUnavailableKey,
            CoverImageResult result
    ) {
        if (!isSupportedImageFile(file)) {
            toast.warning(I18n.t("trip.add.toast.image.unsupported"));
            return;
        }

        result.accept(file.getAbsolutePath());
        selectedImageLabel.setText(file.getName());
        selectedImageLabel.setVisible(true);
        selectedImageLabel.setManaged(true);

        Image image = new Image(file.toURI().toString(), true);
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if (Boolean.TRUE.equals(isError)) {
                coverPreview.setImage(null);
                coverPreview.setViewport(null);
                coverPreview.setVisible(false);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(true);
                uploadPlaceholder.setManaged(true);
                toast.warning(I18n.t(previewUnavailableKey));
            }
        });
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                setCoverPreviewImage(coverPreview, uploadArea, image);
                coverPreview.setVisible(true);
                coverPreview.setManaged(false);
                uploadPlaceholder.setVisible(false);
                uploadPlaceholder.setManaged(false);
            }
        });
    }

    @FunctionalInterface
    public interface CoverImageResult {
        void accept(String absolutePath);
    }

    public static void handleUploadDragOver(DragEvent event, StackPane uploadArea) {
        if (event.getDragboard().hasFiles() && isSupportedImageFile(event.getDragboard().getFiles().getFirst())) {
            event.acceptTransferModes(TransferMode.COPY);
            toggleStyleClass(uploadArea, "editor-upload-area-active", true);
        }
        event.consume();
    }

    public static void handleUploadDragExited(DragEvent event, StackPane uploadArea) {
        toggleStyleClass(uploadArea, "editor-upload-area-active", false);
        event.consume();
    }
}
