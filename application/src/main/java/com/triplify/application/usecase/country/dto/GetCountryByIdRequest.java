package com.triplify.application.usecase.country.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record GetCountryByIdRequest(
        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String id
) {
}
