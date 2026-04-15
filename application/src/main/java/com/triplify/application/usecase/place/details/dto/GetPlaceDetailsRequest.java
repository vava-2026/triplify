package com.triplify.application.usecase.place.details.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record GetPlaceDetailsRequest(
        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String placeId
) {
}
