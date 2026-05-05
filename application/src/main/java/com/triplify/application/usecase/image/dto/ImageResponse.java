package com.triplify.application.usecase.image.dto;

import com.triplify.domain.model.Image;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public record ImageResponse(
        UUID id,
        Path url,
        String description,
        Instant uploadedAt
) {
    public static ImageResponse from(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getUrl(),
                image.getDescription(),
                image.getUploadedAt()
        );
    }
}
