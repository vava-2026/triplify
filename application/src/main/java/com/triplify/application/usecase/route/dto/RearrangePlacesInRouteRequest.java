package com.triplify.application.usecase.route.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RearrangePlacesInRouteRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String routeId,

        List<String> placeIdsInOrder
) {
}
