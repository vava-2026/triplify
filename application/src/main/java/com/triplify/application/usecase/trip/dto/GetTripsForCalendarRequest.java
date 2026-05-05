package com.triplify.application.usecase.trip.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.domain.model.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;

public record GetTripsForCalendarRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        YearMonth yearMonth,

        StatusEnum status
) {
}
