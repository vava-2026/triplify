package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
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

    public Image(@NonNull String url, @NonNull String storageKey) {
        this.id = UUID.randomUUID();
        this.url = url;
        this.storageKey = storageKey;
        this.uploadedAt = Instant.now();
    }

    public void updateDescription(String description) {
        log.debug("Image [{}] description updated.", id);
        setDescription(description);
    }
}