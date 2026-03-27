package com.triplify.application.usecase.triproute.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

import java.util.SortedSet;

public record RearrangeTripRoutesRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String id,

        SortedSet<String> routesIdsInOrder
) {
}
