package com.triplify.domain.model.place;

import java.util.UUID;

public record Country(
        UUID id,
        UUID createdBy,
        String name,
        boolean isBanned
) {
}
