package com.triplify.application.usecase.triproute.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeleteTripRouteRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String tripRouteId
) {
}
