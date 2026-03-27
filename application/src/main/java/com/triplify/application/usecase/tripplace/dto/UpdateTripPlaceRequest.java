package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.domain.model.enums.StatusEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Set;

public record UpdateTripPlaceRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String id,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String placeId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String tripId,

        Instant visitDate
) {
}
