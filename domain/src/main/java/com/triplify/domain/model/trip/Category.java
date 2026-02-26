package com.triplify.domain.model.trip;

import java.util.UUID;

public record Category(
        UUID id,
        UUID createdBy,
        String name,
        String description,
        String icon
) {
}
