package com.triplify.application.usecase.story.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;

public record AddStoryRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String title,

        String description,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        Instant storyTime,
        String tripId,
        String tripRouteId,
        String tripPlaceId,
        String emotionId,
        Set<String> tagIds
) {
}
