package com.triplify.application.usecase.tag.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.model.ColorTheme;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTagRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String userId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String name,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        ColorTheme color
) {
}
