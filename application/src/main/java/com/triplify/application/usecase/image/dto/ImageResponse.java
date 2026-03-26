package com.triplify.application.usecase.image.dto;

import java.time.Instant;

public record ImageResponse(
        String id,
        String url,
        String storageKey,
        String description,
        Instant uploadedAt
) {
}
