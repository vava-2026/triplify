package com.triplify.application.usecase.tag.dto;

import com.triplify.application.model.ColorTheme;

public record TagResponse(
        String id,
        String userId,
        String name,
        ColorTheme color
) {
}
