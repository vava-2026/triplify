package com.triplify.application.usecase.statistic.dto;

import com.triplify.domain.model.enums.StatisticType;

import java.util.UUID;

public record UpdateStatisticDisplayRequest(
        UUID statisticId,
        boolean isDisplayed
) {
}
