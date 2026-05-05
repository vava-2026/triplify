package com.triplify.application.usecase.statistic.dto;

import java.util.UUID;

public record InitializeUserStatisticsRequest(
        UUID userId
) {
}
