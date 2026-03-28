package com.triplify.application.usecase.user.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.domain.model.enums.RoleEnum;

import java.time.Instant;

public record UserResponse(
        String id,
        String username,
        String email,
        RoleEnum role,
        ImageResponse avatar,
        Instant createdAt,
        Instant updatedAt
) {
}
