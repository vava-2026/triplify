package com.triplify.application.usecase.page;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddPageRequest(
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
