package com.triplify.application.usecase.trip.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeleteTripRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String tripId
) {
}
