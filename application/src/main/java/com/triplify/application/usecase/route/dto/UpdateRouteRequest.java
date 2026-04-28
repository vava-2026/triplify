package com.triplify.application.usecase.route.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.nio.file.Path;
import java.util.UUID;

public record UpdateRouteRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id,

        Path coverImage,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.TITLE_MAX_LENGTH, message = ValidationMessage.Constants.TITLE_TOO_LONG)
        String title,

        @Size(max = DtoConstraints.DESCRIPTION_MAX_LENGTH, message = ValidationMessage.Constants.DESCRIPTION_TOO_LONG)
        String description,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        double length
) {
}
