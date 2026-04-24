package com.triplify.application.usecase.story.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.triplify.application.usecase.dto.DtoConstraints;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AddStoryRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.TITLE_MAX_LENGTH, message = ValidationMessage.Constants.TITLE_TOO_LONG)
        String title,

        @Size(max = DtoConstraints.DESCRIPTION_MAX_LENGTH, message = ValidationMessage.Constants.DESCRIPTION_TOO_LONG)
        String description,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        Instant storyTime,
        UUID tripId,
        UUID tripRouteId,
        UUID tripPlaceId,
        UUID emotionId,
        Set<UUID> tagIds
) {
}
