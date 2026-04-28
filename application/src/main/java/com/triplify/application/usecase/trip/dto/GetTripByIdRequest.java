package com.triplify.application.usecase.trip.dto;

import com.triplify.application.shared.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetTripByIdRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id
) {
}
