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
    public static UserResponse from(com.triplify.domain.model.User user) {
        ImageResponse avatar = null;
        if (user.getAvatarImageId() != null) {
            avatar = new ImageResponse(
                    user.getAvatarImageId(),
                    user.getAvatarImage() != null ? user.getAvatarImage().getUrl() : null,
                    user.getAvatarImage() != null ? user.getAvatarImage().getDescription() : null,
                    user.getAvatarImage() != null ? user.getAvatarImage().getUploadedAt() : null
            );
        }

        return new UserResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                avatar,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
