package com.triplify.application.usecase.emotion.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmotionRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String emotionId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String name,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String nameSk,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String emojiUnicode
) {
}
