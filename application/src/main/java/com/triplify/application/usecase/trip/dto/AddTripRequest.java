package com.triplify.application.usecase.trip.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.domain.model.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;

public record AddTripRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String userId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String categoryId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String title,

        String description,
        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        StatusEnum status,
        Instant startedAt,
        Instant endedAt,
        Set<String> tagIds,
        Set<String> imageIds,
        Set<String> countryIds
) {
}
