package com.triplify.application.usecase.category.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String createdById,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String name,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String nameSk,

        String description,

        String descriptionSk,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String emojiUnicode,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String color
) {
}
