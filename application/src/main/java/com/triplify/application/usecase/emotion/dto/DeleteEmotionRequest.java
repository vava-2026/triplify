package com.triplify.application.usecase.emotion.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeleteEmotionRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String emotionId
) {
}
