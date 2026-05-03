package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.domain.model.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateTripPlaceStatusRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        StatusEnum status
) {
}
