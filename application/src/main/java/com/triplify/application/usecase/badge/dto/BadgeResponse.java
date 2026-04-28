package com.triplify.application.usecase.badge.dto;

import com.triplify.application.shared.localization.LocalizedDescription;
import com.triplify.application.shared.localization.LocalizedName;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupType;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.domain.model.Badge;

import java.util.UUID;

public record BadgeResponse(
        UUID id,
        UUID createdById,
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
                badge.getId(),
                badge.getCreatedById(),
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
