package com.triplify.application.usecase.map.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.domain.map.MapObjectType;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record GetClusterItemsRequest(

        @DecimalMin(value = "-90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "90.0", message = ValidationMessage.Constants.LATITUDE_OUT_OF_RANGE)
        double clusterLatitude,

        @DecimalMin(value = "-180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        @DecimalMax(value = "180.0", message = ValidationMessage.Constants.LONGITUDE_OUT_OF_RANGE)
        double clusterLongitude,

        @DecimalMin(value = "2.0", message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        @DecimalMax(value = "18.0", message = ValidationMessage.Constants.NUMBER_MUST_BE_NON_NEGATIVE)
        double zoomLevel,

        Set<MapObjectType> filter,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        @Valid
        PageRequest pageRequest
) {
    public GetClusterItemsRequest {
        if (filter == null) filter = Set.of();
        if (pageRequest == null) pageRequest = PageRequest.defaultRequest();
    }
}
