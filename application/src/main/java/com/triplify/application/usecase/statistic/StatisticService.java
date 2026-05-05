package com.triplify.application.usecase.statistic;

import com.triplify.application.usecase.statistic.dto.*;
import com.triplify.domain.result.Result;

import java.util.List;

public interface StatisticService {

    Result<List<StatisticResponse>> getStatistics(GetStatisticsRequest request);

    Result<List<StatisticResponse>> getDisplayedStatistics(GetDisplayedStatisticsRequest request);

    Result<StatisticResponse> incrementStatistic(IncrementStatisticRequest request);

    Result<Void> initializeUserStatistics(InitializeUserStatisticsRequest request);
}
