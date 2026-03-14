package com.triplify.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@ToString
public class Route {
    @Getter private final UUID id;
    @Getter private final User user;
    @Getter private Image coverImage;
    @Getter private String title;
    @Getter private String description;
    @Getter private Double length;
    @Getter private final Instant createdAt;
    @Getter private Instant updatedAt;
    private final Set<Image> images = new LinkedHashSet<>();

    public Route(@NonNull User user, @NonNull String title, @NonNull String description, @NonNull Double length) throws IllegalArgumentException{
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        if (length < 0) throw new IllegalArgumentException("Route length cannot be negative.");
        this.id = UUID.randomUUID();
        this.user = user;
        this.title = title;
        this.description = description;
        this.length = length;
        this.createdAt = Instant.now();
    }

    public Set<Image> getImages() {
        return Collections.unmodifiableSet(images);
    }

    public void updateTitle(@NonNull String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public void updateDescription(@NonNull String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void updateLength(@NonNull Double length) throws IllegalArgumentException {
        if (length < 0) throw new IllegalArgumentException("Route length cannot be negative.");
        this.length = length;
        this.updatedAt = Instant.now();
    }

    public void updateCoverImage(@NonNull Image coverImage) {
        this.coverImage = coverImage;
        this.updatedAt = Instant.now();
    }

    public void removeCoverImage() {
        this.coverImage = null;
        this.updatedAt = Instant.now();
    }

    public void addImage(@NonNull Image image) {
        this.images.add(image);
        this.updatedAt = Instant.now();
    }

    public void removeImage(@NonNull Image image) {
        this.images.remove(image);
        this.updatedAt = Instant.now();
    }
}
