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
@AllArgsConstructor
public class Trip {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final UUID userId;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private UUID categoryId;

    @Setter(AccessLevel.PRIVATE)
    private Category category;

    @Setter(AccessLevel.PRIVATE)
    private UUID coverImageId;

    @Setter(AccessLevel.PRIVATE)
    private Image coverImage;

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

    private final Set<UUID> tagIds;
    private final Set<UUID> countryIds;
    private final Set<Tag> tags;
    private final Set<Country> countries;

    public Trip(@NonNull UUID userId, @NonNull UUID categoryId, @NonNull String title, @NonNull StatusEnum status) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.tagIds = new HashSet<>();
        this.countryIds = new HashSet<>();
        this.tags = new LinkedHashSet<>();
        this.countries = new LinkedHashSet<>();
    }

    public void updateCoverImage(@NonNull UUID coverImageId) {
        this.coverImageId = coverImageId;
        this.coverImage = null;
        this.updatedAt = Instant.now();
    }

    public void updateCoverImage(@NonNull Image coverImage) {
        this.coverImageId = coverImage.getId();
        this.coverImage = coverImage;
        this.updatedAt = Instant.now();
    }

    public void removeCoverImage() {
        this.coverImageId = null;
        this.coverImage = null;
        this.updatedAt = Instant.now();
    }

    public Set<UUID> getTagIds() {
        return Collections.unmodifiableSet(tagIds);
    }
    public Set<UUID> getCountryIds() {
        return Collections.unmodifiableSet(countryIds);
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public Set<Country> getCountries() {
        return Collections.unmodifiableSet(countries);
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
        setCategory(null);
        setUpdatedAt(Instant.now());
    }

    public void updateCategory(@NonNull Category category) {
        log.debug("Trip [{}] category linked: {}", id, category.getId());
        setCategoryId(category.getId());
        setCategory(category);
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

    public void addTag(@NonNull Tag tag) {
        log.debug("Trip [{}] tag linked: {}", id, tag.getId());
        tagIds.add(tag.getId());
        tags.removeIf(existing -> existing.getId().equals(tag.getId()));
        tags.add(tag);
        setUpdatedAt(Instant.now());
    }

    public void removeTag(@NonNull UUID tagId) {
        log.debug("Trip [{}] tag removed: {}", id, tagId);
        tagIds.remove(tagId);
        tags.removeIf(tag -> tag.getId().equals(tagId));
        setUpdatedAt(Instant.now());
    }

    public void addCountry(@NonNull UUID countryId) {
        log.debug("Trip [{}] country added: {}", id, countryId);
        countryIds.add(countryId);
        setUpdatedAt(Instant.now());
    }

    public void addCountry(@NonNull Country country) {
        log.debug("Trip [{}] country linked: {}", id, country.getId());
        countryIds.add(country.getId());
        countries.removeIf(existing -> existing.getId().equals(country.getId()));
        countries.add(country);
        setUpdatedAt(Instant.now());
    }

    public void removeCountry(@NonNull UUID countryId) {
        log.debug("Trip [{}] country removed: {}", id, countryId);
        countryIds.remove(countryId);
        countries.removeIf(country -> country.getId().equals(countryId));
        setUpdatedAt(Instant.now());
    }
}