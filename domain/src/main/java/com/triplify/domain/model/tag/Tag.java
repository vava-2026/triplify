package com.triplify.domain.model.tag;

import java.util.UUID;

public record Tag(
        UUID id,
        UUID userId,
        String name
) {
}
