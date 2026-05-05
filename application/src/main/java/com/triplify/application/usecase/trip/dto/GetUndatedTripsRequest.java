package com.triplify.application.usecase.trip.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.constraints.NotNull;

public record GetUndatedTripsRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        PageRequest pageRequest,

        StatusEnum status
) {

    public GetUndatedTripsRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }
}
