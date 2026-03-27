package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeleteTripPlaceRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String id
) {
}
