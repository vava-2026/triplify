package com.triplify.application.usecase.country.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.shared.DtoConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCountryRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
        String name,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
        String nameSk,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String emojiUnicode
) {
}
