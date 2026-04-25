package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.shared.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record UpdateTripPlaceRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id,

        Instant visitDate
) {
}
