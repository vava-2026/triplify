package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Route {
    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;
    
    @NonNull
    private final UUID userID;

    @Setter(AccessLevel.PRIVATE)
    private UUID coverImageId;

    @Setter(AccessLevel.PRIVATE)
    private Image coverImage;
    
    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String title;
    
    @Setter(AccessLevel.PRIVATE)
    private String description;
    
    @Setter(AccessLevel.PRIVATE)
    private Double length;
    
    @NonNull
    private final Instant createdAt;
    
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    public Route(@NonNull UUID userID, @NonNull String title, @NonNull String description, @NonNull Double length) throws IllegalArgumentException{
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank.");
        if (length < 0) throw new IllegalArgumentException("Route length cannot be negative.");
        this.id = UUID.randomUUID();
        this.userID = userID;
        this.title = title;
        this.description = description;
        this.length = length;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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
}
