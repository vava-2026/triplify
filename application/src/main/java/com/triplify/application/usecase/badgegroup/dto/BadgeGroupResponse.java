package com.triplify.application.usecase.badgegroup.dto;

import com.triplify.application.localization.LocalizedDescription;
import com.triplify.application.localization.LocalizedName;
import com.triplify.domain.model.BadgeGroup;

public record BadgeGroupResponse(
        String id,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        String createdById
) implements LocalizedName, LocalizedDescription {

    public static BadgeGroupResponse from(BadgeGroup group) {
        return new BadgeGroupResponse(
                group.getId().toString(),
                group.getName(),
                group.getNameSk(),
                group.getDescription(),
                group.getDescriptionSk(),
                group.getCreatedById().toString()
        );
    }
}
