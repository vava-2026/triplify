package com.triplify.infrastructure.storage;

import com.google.inject.Inject;
import com.triplify.domain.service.ImageStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Disk-based implementation of image storage.
 * Stores images in an OS-specific application data directory.
 */
public class DiscImageStorage implements ImageStorageService {
    private static final String APP_FOLDER_NAME = "AppData";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path storageDirectory;

    @Inject
    public DiscImageStorage() {
        this(resolveDefaultStorageDirectory());
    }

    public DiscImageStorage(Path storageDirectory) {
        this.storageDirectory = storageDirectory;
        initializeStorage();
    }

    @Override
    public Path store(Path sourceImage) {
        if (sourceImage == null || !Files.exists(sourceImage)) {
            throw new IllegalArgumentException("Source image path cannot be null and must exist.");
        }

        if (!isAllowedImageType(sourceImage)) {
            throw new IllegalArgumentException("Unsupported image type. Allowed types are: " + ALLOWED_EXTENSIONS);
        }

        initializeStorage();

        String originalFileName = sourceImage.getFileName().toString();
        String extension = getFileExtension(originalFileName);

        String uniqueFileName = UUID.randomUUID() + "." + extension;
        Path targetImagePath = storageDirectory.resolve(uniqueFileName);

        try {
            Files.copy(sourceImage, targetImagePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image to disk at " + targetImagePath, e);
        }

        return targetImagePath;
    }

    @Override
    public void delete(Path storedImage) {
        if (storedImage == null) {
            throw new IllegalArgumentException("Stored image path cannot be null.");
        }

        try {
            Files.deleteIfExists(storedImage);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image at " + storedImage, e);
        }
    }

    private boolean isAllowedImageType(Path imagePath) {
        if (imagePath == null) {
            return false;
        }

        String fileName = imagePath.getFileName().toString();
        String extension = getFileExtension(fileName).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * Resolves the application data directory based on the operating system.
     */
    private static Path resolveDefaultStorageDirectory() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String userHome = System.getProperty("user.home");
        Path basePath;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            basePath = appData != null ? Paths.get(appData) : Paths.get(userHome, "AppData", "Roaming");
        } else if (os.contains("mac")) {
            basePath = Paths.get(userHome, "Library", "Application Support");
        } else {
            basePath = Paths.get(userHome, ".local", "share");
        }

        return basePath.resolve(APP_FOLDER_NAME);
    }

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
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
