package com.triplify.application.usecase.user.dto;

import com.triplify.domain.model.enums.RoleEnum;

import java.time.Instant;

public record UserResponse(
        String id,
        String username,
        String email,
        RoleEnum role,
        String avatarImageId,
        Instant createdAt,
        Instant updatedAt
) {
}
