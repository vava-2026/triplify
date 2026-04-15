package com.triplify.ui.shared.util;

import javafx.scene.image.Image;

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


