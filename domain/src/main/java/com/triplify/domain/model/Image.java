package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Image {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    private final String url;

    @NonNull
    private final String storageKey;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @NonNull
    private final Instant uploadedAt;

    @Builder(builderMethodName = "of")
    private Image(@NonNull String url,
                  @NonNull String storageKey,
                  String description) throws IllegalArgumentException {
        if (url.isBlank()) throw new IllegalArgumentException("Image url must not be blank.");
        if (storageKey.isBlank()) throw new IllegalArgumentException("Storage key must not be blank.");
        this.id = UUID.randomUUID();
        this.url = url;
        this.storageKey = storageKey;
        this.description = description;
        this.uploadedAt = Instant.now();
        log.debug("Image created: id={}, storageKey={}", id, storageKey);
    }

    public void updateDescription(String description) {
        log.debug("Image [{}] description updated.", id);
        setDescription(description);
    }
}