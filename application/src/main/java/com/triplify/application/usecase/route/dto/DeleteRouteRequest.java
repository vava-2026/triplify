package com.triplify.application.usecase.route.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeleteRouteRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String routeId
) {
}
