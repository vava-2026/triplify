package com.triplify.ui.shared.util;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.kordamp.ikonli.javafx.FontIcon;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.toast.ToastService;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public final class EditorUtils {

    private static final int COVER_IMAGE_CACHE_LIMIT = 256;
    private static final int IMAGE_CACHE_LIMIT = 512;
    private static final String COVER_PREVIEW_LISTENER_STATE_KEY = "triplify.coverPreviewListenerState";
    private static final Map<String, Image> COVER_IMAGE_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
                    return size() > COVER_IMAGE_CACHE_LIMIT;
                }
            }
    );
    private static final Map<String, Image> IMAGE_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
                    return size() > IMAGE_CACHE_LIMIT;
                }
            }
    );
    private static final BackgroundSize CARD_COVER_SIZE = new BackgroundSize(1, 1, true, true, false, true);

    private EditorUtils() {}

    public static String safeText(String value) {
        return value == null ? "" : value;
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim();
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

    public static UUID parseUUID(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static String formatMessage(String key, Object... args) {
        return MessageFormat.format(I18n.t(key), args);
    }

    public static boolean isSupportedImageFile(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    public static Image loadImage(
            String imagePath,
            String defaultImage,
            Class<?> resourceContext,
            Double requestedWidth,
            Double requestedHeight,
            Boolean preserveRatio,
            Boolean smooth
    ) {
        boolean hasRequestedSize = requestedWidth != null || requestedHeight != null;
        double width = requestedWidth == null ? 0.0 : requestedWidth;
        double height = requestedHeight == null ? 0.0 : requestedHeight;
        boolean keepRatio = preserveRatio != null && preserveRatio;
        boolean smoothScaling = smooth == null || smooth;

        String source = resolveImageSource(imagePath, defaultImage, resourceContext);
        String cacheKey = buildImageCacheKey(source, width, height, keepRatio, smoothScaling, hasRequestedSize);
        synchronized (IMAGE_CACHE) {
            Image cached = IMAGE_CACHE.get(cacheKey);
            if (cached != null) {
                return cached;
            }

            Image loaded = hasRequestedSize
                    ? new Image(source, width, height, keepRatio, smoothScaling, true)
                    : new Image(source, true);
            IMAGE_CACHE.put(cacheKey, loaded);
            return loaded;
        }
    }

    public static Image loadImage(String imagePath, String defaultImage, Class<?> resourceContext, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth) {
        return loadImage(imagePath, defaultImage, resourceContext,
                Double.valueOf(requestedWidth), Double.valueOf(requestedHeight),
                Boolean.valueOf(preserveRatio), Boolean.valueOf(smooth));
    }

    public static Image loadImage(String imagePath, String defaultImage, Class<?> resourceContext){
        return loadImage(imagePath, defaultImage, resourceContext, null, null, null, null);
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
        removeCoverPreviewImageListeners(coverPreview);
        coverPreview.setImage(image);
        updateCoverViewport(coverPreview, uploadArea);
        if (image == null) {
            coverPreview.setViewport(null);
            return;
        }

        ChangeListener<Number> listener = (obs, o, n) -> updateCoverViewport(coverPreview, uploadArea);
        image.widthProperty().addListener(listener);
        image.heightProperty().addListener(listener);
        image.progressProperty().addListener(listener);
        coverPreview.getProperties().put(COVER_PREVIEW_LISTENER_STATE_KEY, new CoverPreviewListenerState(image, listener));
    }

    // Partly generated by Copilot
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

    public static String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public static Image resolveCoverImage(String coverUrl) {
        if (coverUrl == null || coverUrl.isBlank()) return null;
        synchronized (COVER_IMAGE_CACHE) {
            Image cached = COVER_IMAGE_CACHE.get(coverUrl);
            if (cached != null) {
                return cached;
            }
            Image loaded = new Image(coverUrl, 600, 400, true, true);
            COVER_IMAGE_CACHE.put(coverUrl, loaded);
            return loaded;
        }
    }

    public static void applyCoverBackground(StackPane media, Image image) {
        media.getStyleClass().removeIf(s -> s.startsWith("trip-cover-"));
        if (image != null && !image.isError()) {
            media.setBackground(new Background(new BackgroundImage(
                image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, CARD_COVER_SIZE
            )));
        } else {
            media.setBackground(null);
            media.setStyle(null);
            media.getStyleClass().add("trip-cover-default");
        }
    }

    public static void applyEmojiImage(ImageView view, String emojiUnicode, int size) {
        if (emojiUnicode == null || emojiUnicode.isBlank()) {
            view.setImage(null);
            view.setVisible(false);
            view.setManaged(false);
            return;
        }
        Image img = EmojiUtil.toImage(emojiUnicode.trim(), size);
        boolean show = img != null && !img.isError();
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        view.setVisible(show);
        view.setManaged(show);
        view.setImage(show ? img : null);
    }

    private static void removeCoverPreviewImageListeners(ImageView coverPreview) {
        Object state = coverPreview.getProperties().remove(COVER_PREVIEW_LISTENER_STATE_KEY);
        if (!(state instanceof CoverPreviewListenerState listenerState) || listenerState.image() == null) {
            return;
        }
        listenerState.image().widthProperty().removeListener(listenerState.listener());
        listenerState.image().heightProperty().removeListener(listenerState.listener());
        listenerState.image().progressProperty().removeListener(listenerState.listener());
    }

    private static String resolveImageSource(String imagePath, String defaultImage, Class<?> resourceContext) {
        String resolved = imagePath == null || imagePath.isBlank() ? defaultImage : imagePath;
        if (resolved.startsWith("/")) {
            var resource = resourceContext.getResource(resolved);
            if (resource != null) {
                return resource.toExternalForm();
            }
        }

        if (resolved.startsWith("file:/") || resolved.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            return resolved;
        }

        File file = new File(resolved);
        if (file.exists()) {
            return file.toURI().toString();
        }

        var fallback = resourceContext.getResource(defaultImage);
        return fallback.toExternalForm();
    }

    private static String buildImageCacheKey(
            String source,
            double width,
            double height,
            boolean preserveRatio,
            boolean smooth,
            boolean hasRequestedSize
    ) {
        if (!hasRequestedSize) {
            return source + "|original";
        }
        return source + "|" + width + "|" + height + "|" + preserveRatio + "|" + smooth;
    }

    private record CoverPreviewListenerState(Image image, ChangeListener<Number> listener) {
    }
}
