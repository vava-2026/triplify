package com.triplify.domain.repository;

import com.triplify.domain.model.Image;
import com.triplify.domain.model.enums.ImageOwnerType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository {
    void save(Image image);

    Optional<Image> findById(UUID id);

    Page<Image> findAll(
            UUID userId,
            PageRequest pageRequest,
            String ownerId,
            ImageOwnerType ownerType,
            Instant uploadedFrom,
            Instant uploadedTo,
            boolean uploadTimeAsc
    );

    void update(Image image);

    void delete(UUID id);

    void linkToOwner(UUID imageId, UUID ownerId, ImageOwnerType ownerType);

    void unlinkFromOwner(UUID imageId, UUID ownerId, ImageOwnerType ownerType);
}
