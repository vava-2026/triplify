package com.triplify.infrastructure.storage;

import com.google.inject.Inject;
import com.triplify.domain.service.ImageStorageService;
import com.triplify.domain.util.DefaultDataDirectories;
import com.triplify.infrastructure.repository.BadgeRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Disk-based implementation of image storage.
 * Stores images in an OS-specific application data directory.
 */
public class DiscImageStorage implements ImageStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private static final Logger log = LoggerFactory.getLogger(DiscImageStorage.class);
    private final Path storageDirectory = DefaultDataDirectories.resolve("Triplify").resolve("images");

    @Override
    public Path store(Path sourceImage) {
        log.info("Storing image at {}", sourceImage);
        if (sourceImage == null || !Files.exists(sourceImage)) {
            log.warn("Attempted to store null or non-existent image");
            throw new IllegalArgumentException("Source image path cannot be null and must exist.");
        }

        if (!isAllowedImageType(sourceImage)) {
            log.warn("Attempted to store unsupported image type: {}", sourceImage.getFileName());
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
            log.error("Failed to store image to disk at {}", targetImagePath, e);
            throw new RuntimeException("Failed to store image to disk at " + targetImagePath, e);
        }

        log.info("Stored image at {}", targetImagePath);
        return targetImagePath;
    }

    @Override
    public void delete(Path storedImage) {
        log.info("Deleting image at {}", storedImage);
        if (storedImage == null) {
            log.warn("Attempted to delete null image");
            throw new IllegalArgumentException("Stored image path cannot be null.");
        }

        try {
            Files.deleteIfExists(storedImage);
            log.info("Deleted image at {}", storedImage);
        } catch (IOException e) {
            log.error("Failed to delete image at {}", storedImage, e);
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

    private void initializeStorage() {
        try {
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }
        } catch (IOException e) {
            log.error("Failed to initialize image storage directory at {}", storageDirectory, e);
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
