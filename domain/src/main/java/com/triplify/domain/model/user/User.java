package com.triplify.domain.model.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record User(
        UUID id,
        String username,
        String email,
        String passwordHash,
        String role,
        UUID imageId,
        LocalDateTime createdAt
) {
}
