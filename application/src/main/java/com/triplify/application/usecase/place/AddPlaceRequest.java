package com.triplify.application.usecase.place;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddPlaceRequest(
        @NotNull(message = "validation.field.required")
        Integer tripId,
        @NotBlank(message = "validation.field.required")
        String title,
        @NotBlank(message = "validation.field.required")
        String country,
        String description,
        @NotNull(message = "validation.field.required")
        Double latitude,
        @NotNull(message = "validation.field.required")
        Double longitude,
        String coverImagePath
) {
}
