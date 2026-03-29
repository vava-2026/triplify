package com.triplify.infrastructure.storage;

import com.triplify.domain.service.ImageSaver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;

/**
 * Disk-based implementation of the ImageSaver service.
 * Stores images in the system's application data directory to ensure cross-platform compatibility.
 */
public class DiscImageSaver implements ImageSaver {
    private static final String APP_FOLDER_NAME = "Triplify";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path storageDirectory;

    public DiscImageSaver() {
        this.storageDirectory = determineStorageDirectory();
        initializeStorage();
    }

    @Override
    public Path saveImage(Path sourceImagePath) throws IOException, IllegalArgumentException {
        if (sourceImagePath == null || !Files.exists(sourceImagePath)) {
            throw new IllegalArgumentException("Source image path cannot be null and must exist.");
        }

        if (!isAllowedImageType(sourceImagePath)) {
            throw new IllegalArgumentException("Unsupported image type. Allowed types are: " + ALLOWED_EXTENSIONS);
        }

        String originalFileName = sourceImagePath.getFileName().toString();
        String extension = getFileExtension(originalFileName);

        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
        Path targetImagePath = storageDirectory.resolve(uniqueFileName);

        Files.copy(sourceImagePath, targetImagePath, StandardCopyOption.REPLACE_EXISTING);
        return targetImagePath;
    }

    @Override
    public boolean deleteImage(Path storedImagePath) throws IOException {
        if (storedImagePath == null) {
            return false;
        }
        return Files.deleteIfExists(storedImagePath);
    }

    @Override
    public boolean isAllowedImageType(Path imagePath) {
        if (imagePath == null) {
            return false;
        }

        String fileName = imagePath.getFileName().toString();
        String extension = getFileExtension(fileName).toLowerCase(Locale.ROOT);

        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * Resolves the appropriate application data directory based on the operating system.
     * Windows: %APPDATA%\APP_FOLDER_NAME
     * Mac: ~/Library/Application Support/APP_FOLDER_NAME
     * Linux/Unix: ~/.local/share/APP_FOLDER_NAME (or fallback to ~/.APP_FOLDER_NAME)
     */
    private Path determineStorageDirectory() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String userHome = System.getProperty("user.home");
        Path basePath;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            basePath = appData != null ? Paths.get(appData) : Paths.get(userHome, "AppData", "Roaming");
        } else if (os.contains("mac")) {
            basePath = Paths.get(userHome, "Library", "Application Support");
        } else {
            // Linux/Unix fallback
            basePath = Paths.get(userHome, ".local", "share");
        }

        return basePath.resolve(APP_FOLDER_NAME);
    }

    /**
     * Ensures the storage directory exists on disk.
     */
    private void initializeStorage() {
        try {
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize image storage directory at " + storageDirectory, e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
