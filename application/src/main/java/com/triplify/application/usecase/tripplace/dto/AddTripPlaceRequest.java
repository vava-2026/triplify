package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record AddTripPlaceRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID tripId,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID placeId,

        Instant visitDate,

        TripPlaceSourceType sourceType,

        UUID tripRouteId,

        UUID routePlaceId
) {

    public AddTripPlaceRequest {
        sourceType = sourceType == null ? TripPlaceSourceType.MANUAL : sourceType;
    }
}
