package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record AddTripPlaceRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String tripId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String placeId,

        Instant visitDate
) {
}
