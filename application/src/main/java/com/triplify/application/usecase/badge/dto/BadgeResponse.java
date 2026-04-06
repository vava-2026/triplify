package com.triplify.application.usecase.badge.dto;

import com.triplify.application.localization.LocalizedDescription;
import com.triplify.application.localization.LocalizedName;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;

public record BadgeResponse(
        String id,
        String createdById,
        BadgeGroupResponse group,
        ImageResponse image,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        int level,
        int requiredValue
) implements LocalizedName, LocalizedDescription {
}
