package com.triplify.domain.model.badge;

import java.util.UUID;

public record Badge(
        UUID id,
        UUID createdBy,
        UUID imageId,
        UUID groupId,
        String name,
        String description,
        int level,
        int requiredValue
) {
}
