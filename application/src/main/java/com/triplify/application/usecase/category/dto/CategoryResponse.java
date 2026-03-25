package com.triplify.application.usecase.category.dto;

import com.triplify.application.model.ColorTheme;

public record CategoryResponse(
        String id,
        String createdById,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        String emojiUnicode,
        ColorTheme color
) {
}
