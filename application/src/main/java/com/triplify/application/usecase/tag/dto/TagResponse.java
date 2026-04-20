package com.triplify.application.usecase.tag.dto;

import com.triplify.application.model.ColorTheme;

import java.util.UUID;

public record TagResponse(
        UUID id,
        UUID userId,
        String name,
        ColorTheme color
) {
}
