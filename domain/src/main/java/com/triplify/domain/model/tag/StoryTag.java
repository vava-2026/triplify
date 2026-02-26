package com.triplify.domain.model.tag;

import java.util.UUID;

public record StoryTag(
        UUID storyId,
        UUID tagId
) {
}
