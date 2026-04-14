package com.triplify.application.usecase.tripplace.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record AddTripPlaceRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String tripId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String placeId,

        Instant visitDate,

        TripPlaceSourceType sourceType,

        String tripRouteId,

        String routePlaceId
) {

    public AddTripPlaceRequest {
        sourceType = sourceType == null ? TripPlaceSourceType.MANUAL : sourceType;
        tripRouteId = tripRouteId == null ? null : tripRouteId.trim();
        routePlaceId = routePlaceId == null ? null : routePlaceId.trim();
    }
}
