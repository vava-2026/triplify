package com.triplify.application.usecase.tag.dto;

import com.triplify.application.shared.ColorTheme;
import com.triplify.domain.model.Tag;

import java.util.UUID;

public record TagResponse(
        UUID id,
        UUID userId,
        String name,
        ColorTheme color
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getUserId(),
                tag.getName(),
                ColorTheme.from(tag.getColor())
        );
    }
}
