package com.triplify.application.usecase.place.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.nio.file.Path;
import java.util.UUID;

public record AddPlaceRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID countryId,

        Path coverImage,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.TITLE_MAX_LENGTH, message = ValidationMessage.Constants.TITLE_TOO_LONG)
        String title,

        @Size(max = DtoConstraints.DESCRIPTION_MAX_LENGTH, message = ValidationMessage.Constants.DESCRIPTION_TOO_LONG)
        String description,
        @DecimalMin(value = "-90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        double latitude,

        @DecimalMin(value = "-180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        double longitude
) {
}
