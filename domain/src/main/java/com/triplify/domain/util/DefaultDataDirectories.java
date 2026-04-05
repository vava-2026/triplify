package com.triplify.domain.util;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public final class DefaultDataDirectories {

    private DefaultDataDirectories() {
    }

    public static Path resolve(String firstSegment, String... moreSegments) {
        Objects.requireNonNull(firstSegment, "firstSegment");

        Path directory = resolveBaseDirectory().resolve(firstSegment);
        if (moreSegments != null) {
            for (String segment : moreSegments) {
                directory = directory.resolve(Objects.requireNonNull(segment, "moreSegments element"));
            }
        }
        return directory;
    }

    private static Path resolveBaseDirectory() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                return Path.of(appData);
            }
            return Path.of(userHome, "AppData", "Roaming");
        }

        if (os.contains("mac")) {
            return Path.of(userHome, "Library", "Application Support");
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            return Path.of(xdgDataHome);
        }
        return Path.of(userHome, ".local", "share");
    }
}

