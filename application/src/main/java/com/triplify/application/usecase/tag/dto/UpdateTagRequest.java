package com.triplify.application.usecase.tag.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.shared.ColorTheme;
import com.triplify.application.usecase.dto.DtoConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateTagRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
        String name,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        ColorTheme color
) {
}
