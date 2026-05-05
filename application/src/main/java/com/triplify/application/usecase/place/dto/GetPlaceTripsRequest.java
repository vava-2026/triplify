package com.triplify.application.usecase.place.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetPlaceTripsRequest(
        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID placeId,
        PageRequest pageRequest
) {
    public GetPlaceTripsRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }
}
