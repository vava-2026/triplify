package com.triplify.application.usecase.statistic.dto;

import com.triplify.domain.model.enums.StatisticType;

import java.util.UUID;

public record IncrementStatisticRequest(
        UUID userId,
        StatisticType type,
        long amount
) {
    public IncrementStatisticRequest(UUID userId, StatisticType type) {
        this(userId, type, 1);
    }
}
