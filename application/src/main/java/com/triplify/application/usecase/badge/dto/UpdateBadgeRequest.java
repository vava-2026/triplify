package com.triplify.application.usecase.badge.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateBadgeRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String badgeId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String groupId,

        String imageId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String name,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String nameSk,

        String description,

        String descriptionSk,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        int level,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        int requiredValue
) {
}
