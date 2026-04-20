package com.triplify.application.usecase.triproute.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetTripRouteByIdRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id
) {
}
