package com.triplify.application.usecase.category.dto;

import com.triplify.application.shared.localization.LocalizedDescription;
import com.triplify.application.shared.localization.LocalizedName;
import com.triplify.application.shared.ColorTheme;
import com.triplify.domain.model.Category;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID createdById,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        String emojiUnicode,
        ColorTheme color
) implements LocalizedName, LocalizedDescription {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getCreatedById(),
                c.getName(),
                c.getNameSk(),
                c.getDescription(),
                c.getDescriptionSk(),
                c.getEmojiUnicode(),
                ColorTheme.from(c.getColor())
        );
    }
}
