package com.triplify.application.usecase.tripplace.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReplaceManualTripPlacesRequest(
        @NotNull UUID tripId,
        @NotNull List<UUID> placeIds
) {
}
