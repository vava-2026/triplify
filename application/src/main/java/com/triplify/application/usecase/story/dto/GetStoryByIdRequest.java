package com.triplify.application.usecase.story.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record GetStoryByIdRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        UUID id
) {
}
