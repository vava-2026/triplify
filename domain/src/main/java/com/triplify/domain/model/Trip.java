package com.triplify.domain.model;

import com.triplify.domain.model.enums.StatusEnum;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@ToString
public class Trip {
    @Getter private final UUID id;
    @Getter private final User user;
    @Getter private Category category;
    @Getter private String title;
    @Getter private String description;
    @Getter private StatusEnum status;
    @Getter private Instant startedAt;
    @Getter private Instant endedAt;
    @Getter private final Instant createdAt;
    @Getter private Instant updatedAt;
    private final Set<Tag> tags = new HashSet<>();
    private final Set<Image> images = new LinkedHashSet<>();
    private final Set<Country> countries = new HashSet<>();

    public Trip(@NonNull User user, @NonNull String title, @NonNull String description, @NonNull Category category) throws IllegalArgumentException {
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        this.id = UUID.randomUUID();
        this.user = user;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = StatusEnum.PLANNED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public Set<Image> getImages() {
        return Collections.unmodifiableSet(images);
    }

    public Set<Country> getCountries() {
        return Collections.unmodifiableSet(countries);
    }

    public void updateTitle(@NonNull String newTitle) {
        if (newTitle.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        this.title = newTitle;
        this.updatedAt = Instant.now();
    }

    public void updateDescription(@NonNull String newDescription) {
        this.description = newDescription;
        this.updatedAt = Instant.now();
    }

    public void updateCategory(@NonNull Category newCategory) {
        this.category = newCategory;
        this.updatedAt = Instant.now();
    }

    public void start(@NonNull Instant startedAt) {
        if (this.status != StatusEnum.PLANNED) {
            throw new IllegalStateException("Only planned trips can be started.");
        }
        this.status = StatusEnum.ONGOING;
        this.startedAt = startedAt;
        this.updatedAt = Instant.now();
    }

    public void complete(@NonNull Instant endedAt) throws IllegalArgumentException, IllegalStateException {
        if (this.status != StatusEnum.ONGOING) {
            throw new IllegalStateException("Only ongoing trips can be completed.");
        }
        if (this.startedAt != null && endedAt.isBefore(this.startedAt)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        this.status = StatusEnum.VISITED;
        this.endedAt = endedAt;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.status == StatusEnum.VISITED) {
            throw new IllegalStateException("Completed trips cannot be cancelled.");
        }
        this.status = StatusEnum.CANCELED;
        this.updatedAt = Instant.now();
    }

    public void addTag(@NonNull Tag tag) {
        this.tags.add(tag);
        this.updatedAt = Instant.now();
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        this.updatedAt = Instant.now();
    }

    public void addImage(@NonNull Image image) {
        this.images.add(image);
        this.updatedAt = Instant.now();
    }

    public void removeImage(Image image) {
        this.images.remove(image);
        this.updatedAt = Instant.now();
    }

    public void addCountry(@NonNull Country country) {
        this.countries.add(country);
        this.updatedAt = Instant.now();
    }

    public void removeCountry(Country country) {
        this.countries.remove(country);
        this.updatedAt = Instant.now();
    }
}
