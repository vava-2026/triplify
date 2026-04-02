package com.triplify.application.usecase.image.dto;

import java.nio.file.Path;
import java.time.Instant;

public record ImageResponse(
        String id,
        Path url,
        String description,
        Instant uploadedAt
) {
}
