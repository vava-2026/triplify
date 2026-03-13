package com.triplify.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Image {
    private final UUID id;
    private final String url;
    private final String storageKey;
    private String description;
    private final Instant uploadedAt;

    private Image(String url, String storageKey, String description) {
        super();
        if (url == null || url.isBlank()) throw new IllegalArgumentException("Image url must not be blank.");
        if (storageKey == null || storageKey.isBlank()) throw new IllegalArgumentException("Storage key must not be blank.");
        this.url = url;
        this.storageKey = storageKey;
        this.description = description;
        this.uploadedAt = Instant.now();
    }

    public static Image create(String url, String storageKey, String description) {
        return new Image(url, storageKey, description);
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
