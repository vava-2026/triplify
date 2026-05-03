package com.triplify.application.usecase.statistic.dto;

import java.util.UUID;

public record GetStatisticsRequest(
        UUID userId
) {
}
