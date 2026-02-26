package com.triplify.domain.model.user;

import java.util.UUID;

public record UserBadge(
        UUID id,
        UUID userId,
        UUID badgeId,
        int progressValue,
        boolean isUnlocked
) {
}
