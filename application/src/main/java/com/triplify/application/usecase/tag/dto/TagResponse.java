package com.triplify.application.usecase.tag.dto;

import com.triplify.application.model.ColorTheme;
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
                tag.getId().toString(),
                tag.getUserId().toString(),
                tag.getName(),
                ColorTheme.from(tag.getColor())
        );
    }
}
