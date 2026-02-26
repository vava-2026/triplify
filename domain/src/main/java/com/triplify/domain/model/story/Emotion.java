package com.triplify.domain.model.story;

import java.util.UUID;

public record Emotion(
        UUID id,
        UUID createdBy,
        String name,
        String icon
) {
}
