package com.triplify.ui.shared.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.file.Path;

public final class AvatarImageHelper {

    private static final String DEFAULT_INITIAL = "?";

    private AvatarImageHelper() {
    }

    public static String extractInitial(String username) {
        if (username == null || username.isBlank()) {
            return DEFAULT_INITIAL;
        }
        return username.substring(0, 1).toUpperCase();
    }

    public static Image resolveAvatarImage(Path imagePath) {
        return loadFromPath(imagePath);
    }

    public static void applyCoverSquare(ImageView imageView, Image image, double size) {
        if (imageView == null) {
            return;
        }

        imageView.setImage(image);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setCache(false);

        if (image == null) {
            imageView.setViewport(null);
            return;
        }

        applySquareViewport(imageView, image);
        if (image.getProgress() < 1.0) {
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() >= 1.0) {
                    applySquareViewport(imageView, image);
                }
            });
        }
    }

    private static void applySquareViewport(ImageView imageView, Image image) {
        double width = image.getWidth();
        double height = image.getHeight();
        if (width <= 0 || height <= 0) {
            imageView.setViewport(null);
            return;
        }

        double side = Math.min(width, height);
        double x = (width - side) / 2.0;
        double y = (height - side) / 2.0;
        imageView.setViewport(new Rectangle2D(x, y, side, side));
    }

    private static Image loadFromPath(Path imagePath) {
        if (imagePath == null) {
            return null;
        }
        return loadFromUrl(imagePath.toUri().toString());
    }

    private static Image loadFromUrl(String url) {
        try {
            Image image = new Image(url, true);
            return image.isError() ? null : image;
        } catch (RuntimeException ex) {
            return null;
        }
    }
}


