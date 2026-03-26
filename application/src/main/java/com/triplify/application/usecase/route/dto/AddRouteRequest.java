package com.triplify.application.usecase.route.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddRouteRequest(

        String coverImageId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String title,

        String description,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        double length
) {
}
