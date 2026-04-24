package com.triplify.application.usecase.category.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.shared.ColorTheme;
import com.triplify.application.usecase.dto.DtoConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddCategoryRequest(

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

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String emojiUnicode,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        ColorTheme color
) {
}
