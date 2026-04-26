package com.triplify.application.usecase.story.dto;

import com.triplify.application.shared.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record DeleteStoryRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        UUID id
) {
}
