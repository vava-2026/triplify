package com.triplify.domain.service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Service interface for managing image storage.
 */
public interface ImageSaver {

    /**
     * Saves an image from the given source path to the application's storage.
     *
     * @param sourceImagePath The path of the original image file to be saved.
     * @return The path where the image was stored in the application data.
     * @throws IllegalArgumentException If the image type is not allowed or the file is invalid.
     * @throws IOException If an error occurs during the file copying process.
     */
    Path saveImage(Path sourceImagePath) throws IOException, IllegalArgumentException;

    /**
     * Deletes an image from the application's storage.
     *
     * @param storedImagePath The path of the stored image to be deleted.
     * @return true if the image was successfully deleted, false otherwise.
     * @throws IOException If an error occurs during the deletion process.
     */
    boolean deleteImage(Path storedImagePath) throws IOException;

    /**
     * Validates if the given file path points to an allowed image type.
     *
     * @param imagePath The path of the image to validate.
     * @return true if the image type is allowed, false otherwise.
     */
    boolean isAllowedImageType(Path imagePath);
}
