package com.triplify.application.usecase.trip.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.domain.model.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record UpdateTripStatusRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        StatusEnum status,

        Instant startedAt,
        Instant endedAt
) {
}
