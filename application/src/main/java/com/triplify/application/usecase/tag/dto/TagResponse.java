package com.triplify.application.usecase.tag.dto;

import com.triplify.application.model.ColorTheme;
import com.triplify.domain.model.Tag;

public record TagResponse(
        String id,
        String userId,
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
