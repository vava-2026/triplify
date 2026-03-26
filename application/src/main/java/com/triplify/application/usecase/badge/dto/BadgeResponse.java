package com.triplify.application.usecase.badge.dto;

public record BadgeResponse(
        String id,
        String createdById,
        String groupId,
        String imageId,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        int level,
        int requiredValue
) {
}
