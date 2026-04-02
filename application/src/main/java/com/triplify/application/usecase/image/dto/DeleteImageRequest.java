package com.triplify.application.usecase.image.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeleteImageRequest(
        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String id
) {
}
