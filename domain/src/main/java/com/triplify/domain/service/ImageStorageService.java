package com.triplify.domain.service;

import java.nio.file.Path;

/**
 * Stores and removes image files in the application's local storage.
 * <p>
 * This port only manages physical files. It does not know about image entities,
 * database tables, ownership, or application-level metadata.
 * <p>
 * It should be used directly only by the application image service.
 */
public interface ImageStorageService {

    /**
     * Copies the given source image into the application's local storage.
     *
     * @param sourceImage path to the source image selected by the user
     * @return path to the stored image inside application-managed storage
     */
    Path store(Path sourceImage);

    /**
     * Removes a previously stored image from application-managed storage.
     *
     * @param storedImage path to the stored image inside application-managed storage
     */
    void delete(Path storedImage);
}
