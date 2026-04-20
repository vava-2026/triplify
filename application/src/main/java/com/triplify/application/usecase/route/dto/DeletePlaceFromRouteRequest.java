package com.triplify.application.usecase.route.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeletePlaceFromRouteRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID routeId,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID placeId
) {
}

