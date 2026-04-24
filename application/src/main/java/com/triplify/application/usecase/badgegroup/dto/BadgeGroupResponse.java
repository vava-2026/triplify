package com.triplify.application.usecase.badgegroup.dto;

import com.triplify.application.localization.LocalizedDescription;
import com.triplify.application.localization.LocalizedName;
import com.triplify.domain.model.BadgeGroup;

import java.util.UUID;

public record BadgeGroupResponse(
        UUID id,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        UUID createdById
) implements LocalizedName, LocalizedDescription {

    public static BadgeGroupResponse from(BadgeGroup group) {
        return new BadgeGroupResponse(
                group.getId(),
                group.getName(),
                group.getNameSk(),
                group.getDescription(),
                group.getDescriptionSk(),
                group.getCreatedById()
        );
    }
}
