package com.triplify.application.usecase.image.dto;

import com.triplify.application.shared.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteImageRequest(
        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id
) {
}
