package com.triplify.application.usecase.trip.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.domain.model.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateTripStatusRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String id,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        StatusEnum status,

        Instant startedAt,
        Instant endedAt
) {
}
