package com.triplify.application.usecase.badge.dto;

import com.triplify.application.localization.LocalizedDescription;
import com.triplify.application.localization.LocalizedName;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupType;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.domain.model.Badge;

public record BadgeResponse(
        String id,
        String createdById,
        BadgeGroupType group,
        ImageResponse image,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        int level,
        int requiredValue
) implements LocalizedName, LocalizedDescription {

    public static BadgeResponse from(Badge badge, ImageResponse image) {
        return new BadgeResponse(
                badge.getId().toString(),
                badge.getCreatedById().toString(),
                BadgeGroupType.fromIdOrThrow(badge.getGroupId().toString()),
                image,
                badge.getName(),
                badge.getNameSk(),
                badge.getDescription(),
                badge.getDescriptionSk(),
                badge.getLevel(),
                badge.getRequiredValue()
        );
    }
}
