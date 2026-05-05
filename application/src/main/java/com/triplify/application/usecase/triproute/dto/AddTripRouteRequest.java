package com.triplify.application.usecase.triproute.dto;

import com.triplify.application.shared.error.ValidationMessage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddTripRouteRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID tripId,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID routeId,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        int order
) {
}
