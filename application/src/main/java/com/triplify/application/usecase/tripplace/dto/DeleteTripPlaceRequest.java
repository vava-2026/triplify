package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteTripPlaceRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id
) {
}
