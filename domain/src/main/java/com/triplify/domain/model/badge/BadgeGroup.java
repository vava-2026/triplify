package com.triplify.domain.model.badge;

import java.util.UUID;

public record BadgeGroup(
        UUID id,
        UUID createdBy,
        String name,
        String description
) {
}
