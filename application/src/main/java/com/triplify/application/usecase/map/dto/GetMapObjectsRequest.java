package com.triplify.application.usecase.map.dto;

import com.triplify.application.shared.error.ValidationMessage;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public record GetMapObjectsRequest(

        @DecimalMin(value = "-90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        Double latitude,

        @DecimalMin(value = "-180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        Double longitude,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        int tileRadius,

        @Min(value = 0, message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        int zLevel,

        Filter filter
) {

    public GetMapObjectsRequest {
        filter = filter == null ? new Filter(Filter.MapObjectType.ALL) : filter;
    }

    public record Filter(
            MapObjectType objectType
    ) {

        public enum MapObjectType {
            ALL,
            ROUTE,
            PLACE,
            STORY,
            TRIP
        }

        public Filter {
            objectType = objectType == null ? MapObjectType.ALL : objectType;
        }
    }
}
