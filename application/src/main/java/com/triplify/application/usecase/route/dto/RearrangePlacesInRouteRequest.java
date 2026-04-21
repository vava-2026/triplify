package com.triplify.application.usecase.route.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record RearrangePlacesInRouteRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id,

        List<UUID> placeIdsInOrder
) {
}
