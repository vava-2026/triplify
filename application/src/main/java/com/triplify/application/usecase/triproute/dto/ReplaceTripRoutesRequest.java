package com.triplify.application.usecase.triproute.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReplaceTripRoutesRequest(
        @NotNull UUID tripId,
        @NotNull List<UUID> routeIdsInOrder
) {
}
