package com.triplify.application.usecase.statistic.dto;

import com.triplify.domain.model.enums.StatisticType;
import com.triplify.domain.model.Statistic;

import java.util.UUID;

public record StatisticResponse(
        UUID id,
        UUID userId,
        StatisticType type,
        long amount,
        boolean isDisplayed,
        String icon,
    String labelKey,
    String badgeGroupId
) {
    public static StatisticResponse from(Statistic statistic) {
        return new StatisticResponse(
                statistic.getId(),
                statistic.getUserId(),
                statistic.getType(),
                statistic.getAmount(),
                statistic.isDisplayed(),
                statistic.getType().getIcon(),
                statistic.getType().getLabelKey(),
                statistic.getType().getBadgeGroupId()
        );
    }
}
