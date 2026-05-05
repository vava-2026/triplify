package com.triplify.application.usecase.triproute.dto;

import com.triplify.application.shared.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.SortedSet;
import java.util.UUID;

public record RearrangeTripRoutesRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id,

        SortedSet<UUID> routesIdsInOrder
) {
}
