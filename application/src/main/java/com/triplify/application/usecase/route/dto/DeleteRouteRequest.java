package com.triplify.application.usecase.route.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteRouteRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id
) {
}
