package com.triplify.application.usecase.place.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetPlaceDetailsRequest(
        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID placeId
) {
}
