package com.triplify.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class Image {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id = UUID.randomUUID();

    @NonNull
    private final String url;

    @NonNull
    private final String storageKey;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @NonNull
    private final Instant uploadedAt;

    public void updateDescription(String description) {
        log.debug("Image [{}] description updated.", id);
        setDescription(description);
    }
}