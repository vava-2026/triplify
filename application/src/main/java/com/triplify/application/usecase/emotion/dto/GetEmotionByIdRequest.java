package com.triplify.application.usecase.emotion.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetEmotionByIdRequest(
        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id
) {
}
