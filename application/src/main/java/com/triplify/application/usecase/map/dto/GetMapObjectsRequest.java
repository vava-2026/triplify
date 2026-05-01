package com.triplify.application.usecase.map.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.domain.map.MapObjectType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.util.Set;

public record GetMapObjectsRequest(

        @DecimalMin(value = "-90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        double minLatitude,

        @DecimalMin(value = "-180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        double minLongitude,

        @DecimalMin(value = "-90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        double maxLatitude,

        @DecimalMin(value = "-180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        double maxLongitude,

        @DecimalMin(value = "2.0", message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        @DecimalMax(value = "18.0", message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        double zoomLevel,

        Set<MapObjectType> filter
) {
    public GetMapObjectsRequest {
        if (filter == null) filter = Set.of();
    }
}
