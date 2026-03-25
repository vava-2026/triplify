package com.triplify.application.usecase.country.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record UpdateCountryRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String countryId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String name,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String nameSk,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String emojiUnicode
) {
}

