package com.triplify.application.usecase.category.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeleteCategoryRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String categoryId
) {
}
