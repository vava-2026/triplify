package com.triplify.application.usecase.place.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record UpdatePlaceRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String placeId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String countryId,

        String coverImageId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String title,

        String description,
        @DecimalMin(value = "-90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        double latitude,

        @DecimalMin(value = "-180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        double longitude
) {
}
