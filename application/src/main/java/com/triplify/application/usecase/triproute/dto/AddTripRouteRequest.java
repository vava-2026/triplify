package com.triplify.application.usecase.triproute.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record AddTripRouteRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String tripId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String routeId,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        int order
) {
}
