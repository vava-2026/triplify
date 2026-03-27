package com.triplify.application.usecase.tag.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.model.ColorTheme;
import com.triplify.application.usecase.dto.DtoConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTagRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String tagId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
        String name,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        ColorTheme color
) {
}
