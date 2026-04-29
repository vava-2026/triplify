package com.triplify.domain.repository;

import com.triplify.domain.model.Image;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository {
    void save(Image image);

    Optional<Image> findById(UUID id);

    /**
     * Returns a paged list of all images, optionally filtered by upload time and sorted.
     *
     * @param pageRequest pagination parameters
     * @param ownerId image owner id; requires ownerType when provided
     * @param ownerType owner entity type (TRIP, TRIP_ROUTE, TRIP_PLACE, STORY)
     * @param uploadedFrom inclusive lower bound; null means no lower bound
     * @param uploadedTo   inclusive upper bound; null means no upper bound
     * @param uploadTimeAsc true for oldest-first, false for newest-first
     */
    Page<Image> findAll(
            PageRequest pageRequest,
            String ownerId,
            String ownerType,
            Instant uploadedFrom,
            Instant uploadedTo,
            boolean uploadTimeAsc
    );

    void update(Image image);

    void delete(UUID id);

    void linkToOwner(UUID imageId, UUID ownerId, String ownerType);

    void unlinkFromOwner(UUID imageId, UUID ownerId, String ownerType);
}
