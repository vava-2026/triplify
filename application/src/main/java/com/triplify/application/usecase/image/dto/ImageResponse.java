package com.triplify.application.usecase.image.dto;

import com.triplify.domain.model.Image;

import java.nio.file.Path;
import java.time.Instant;

public record ImageResponse(
        String id,
        Path url,
        String description,
        Instant uploadedAt
) {
    public static ImageResponse from(Image image) {
        return new ImageResponse(
                image.getId().toString(),
                image.getUrl(),
                image.getDescription(),
                image.getUploadedAt()
        );
    }
}
