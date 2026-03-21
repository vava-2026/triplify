package com.triplify.domain.model;

import com.triplify.domain.model.enums.StatusEnum;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Trip {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final UUID userId;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private UUID categoryId;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String title;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private StatusEnum status;

    @Setter(AccessLevel.PRIVATE)
    private Instant startedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant endedAt;

    @NonNull
    private final Instant createdAt;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    private final Set<UUID> tagIds = new HashSet<>();
    private final Set<UUID> imageIds = new LinkedHashSet<>();
    private final Set<UUID> countryIds = new HashSet<>();

    @Builder(builderMethodName = "of")
    private Trip(@NonNull UUID userId,
                 @NonNull UUID categoryId,
                 @NonNull String title,
                 String description) {
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.status = StatusEnum.PLANNED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        log.debug("Trip created: id={}, title={}, userId={}", id, title, userId);
    }

    public Set<UUID> getTagIds() {
        return Collections.unmodifiableSet(tagIds);
    }

    public Set<UUID> getImageIds() {
        return Collections.unmodifiableSet(imageIds);
    }

    public Set<UUID> getCountryIds() {
        return Collections.unmodifiableSet(countryIds);
    }

    public void updateTitle(@NonNull String title) {
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        log.debug("Trip [{}] title: {} to {}", id, this.title, title);
        setTitle(title);
        setUpdatedAt(Instant.now());
    }

    public void updateDescription(String description) {
        log.debug("Trip [{}] description updated.", id);
        setDescription(description);
        setUpdatedAt(Instant.now());
    }

    public void updateCategory(@NonNull UUID categoryId) {
        log.debug("Trip [{}] category: {} to {}", id, this.categoryId, categoryId);
        setCategoryId(categoryId);
        setUpdatedAt(Instant.now());
    }

    public void start(@NonNull Instant startedAt) {
        if (this.status != StatusEnum.PLANNED) {
            throw new IllegalStateException("Only planned trips can be started.");
        }
        log.debug("Trip [{}] started at {}", id, startedAt);
        setStatus(StatusEnum.ONGOING);
        setStartedAt(startedAt);
        setUpdatedAt(Instant.now());
    }

    public void complete(@NonNull Instant endedAt) {
        if (this.status != StatusEnum.ONGOING) {
            throw new IllegalStateException("Only ongoing trips can be completed.");
        }
        if (this.startedAt != null && endedAt.isBefore(this.startedAt)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        log.debug("Trip [{}] completed at {}", id, endedAt);
        setStatus(StatusEnum.VISITED);
        setEndedAt(endedAt);
        setUpdatedAt(Instant.now());
    }

    public void cancel() {
        if (this.status == StatusEnum.VISITED) {
            throw new IllegalStateException("Completed trips cannot be cancelled.");
        }
        log.debug("Trip [{}] cancelled.", id);
        setStatus(StatusEnum.CANCELED);
        setUpdatedAt(Instant.now());
    }

    public void addTag(@NonNull UUID tagId) {
        log.debug("Trip [{}] tag added: {}", id, tagId);
        tagIds.add(tagId);
        setUpdatedAt(Instant.now());
    }

    public void removeTag(@NonNull UUID tagId) {
        log.debug("Trip [{}] tag removed: {}", id, tagId);
        tagIds.remove(tagId);
        setUpdatedAt(Instant.now());
    }

    public void addImage(@NonNull UUID imageId) {
        log.debug("Trip [{}] image added: {}", id, imageId);
        imageIds.add(imageId);
        setUpdatedAt(Instant.now());
    }

    public void removeImage(@NonNull UUID imageId) {
        log.debug("Trip [{}] image removed: {}", id, imageId);
        imageIds.remove(imageId);
        setUpdatedAt(Instant.now());
    }

    public void addCountry(@NonNull UUID countryId) {
        log.debug("Trip [{}] country added: {}", id, countryId);
        countryIds.add(countryId);
        setUpdatedAt(Instant.now());
    }

    public void removeCountry(@NonNull UUID countryId) {
        log.debug("Trip [{}] country removed: {}", id, countryId);
        countryIds.remove(countryId);
        setUpdatedAt(Instant.now());
    }
}