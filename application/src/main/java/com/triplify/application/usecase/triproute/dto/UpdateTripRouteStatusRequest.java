package com.triplify.application.usecase.triproute.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.domain.model.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record UpdateTripRouteStatusRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        StatusEnum status,

        Instant startedAt,
        Instant endedAt
) {
}
