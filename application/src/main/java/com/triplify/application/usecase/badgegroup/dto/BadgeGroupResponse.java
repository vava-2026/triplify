package com.triplify.application.usecase.badgegroup.dto;

public record BadgeGroupResponse(
        String id,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        String createdById
) {
}
