package com.triplify.application.usecase.badge.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;

public record BadgeResponse(
        String id,
        String createdById,
        String groupId,
        ImageResponse image,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        int level,
        int requiredValue
) {
}
