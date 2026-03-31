package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record UpdateTripPlaceRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String id,

        Instant visitDate
) {
}
