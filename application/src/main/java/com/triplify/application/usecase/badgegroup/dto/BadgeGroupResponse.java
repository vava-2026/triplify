package com.triplify.application.usecase.badgegroup.dto;

import com.triplify.application.localization.LocalizedDescription;
import com.triplify.application.localization.LocalizedName;

public record BadgeGroupResponse(
        String id,
        String name,
        String nameSk,
        String description,
        String descriptionSk,
        String createdById
) implements LocalizedName, LocalizedDescription {
}
