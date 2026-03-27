package com.triplify.application.usecase.badge.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBadgeRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String badgeId,

        String imageId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
        String name,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
        String nameSk,

        @Size(max = DtoConstraints.DESCRIPTION_MAX_LENGTH, message = ValidationMessage.Constants.DESCRIPTION_TOO_LONG)
        String description,

        @Size(max = DtoConstraints.DESCRIPTION_MAX_LENGTH, message = ValidationMessage.Constants.DESCRIPTION_TOO_LONG)
        String descriptionSk,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        int level,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        int requiredValue
) {
}
