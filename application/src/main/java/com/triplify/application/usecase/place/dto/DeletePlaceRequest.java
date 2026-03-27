package com.triplify.application.usecase.place.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeletePlaceRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String id
) {
}
