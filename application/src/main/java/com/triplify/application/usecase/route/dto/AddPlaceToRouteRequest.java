package com.triplify.application.usecase.route.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record AddPlaceToRouteRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String routeId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String placeId
) {
}

