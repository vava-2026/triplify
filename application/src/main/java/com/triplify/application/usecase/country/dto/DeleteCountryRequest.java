package com.triplify.application.usecase.country.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteCountryRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id
) {
}
