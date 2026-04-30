package com.triplify.application.usecase.statistic;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.statistic.dto.*;
import com.triplify.domain.error.StatisticError;
import com.triplify.domain.model.Statistic;
import com.triplify.domain.model.enums.StatisticType;
import com.triplify.domain.repository.StatisticRepository;
import com.triplify.domain.result.Result;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Authenticated
public class StatisticServiceImpl implements StatisticService {
    private static final Logger log = LoggerFactory.getLogger(StatisticServiceImpl.class);
    private final StatisticRepository statisticRepository;
    private final UserSessionContext sessionContext;

    @Inject
    public StatisticServiceImpl(StatisticRepository statisticRepository, UserSessionContext sessionContext) {
        this.statisticRepository = statisticRepository;
        this.sessionContext = sessionContext;
    }

    @Override
    public Result<List<StatisticResponse>> getStatistics(GetStatisticsRequest request) {
        log.info("Getting statistics for userId='{}'", request.userId());
        List<Statistic> statistics = statisticRepository.findByUserId(request.userId());
        List<StatisticResponse> responses = statistics.stream()
                .map(StatisticResponse::from)
                .toList();
        log.info("Retrieved {} statistics for userId='{}'", responses.size(), request.userId());
        return Result.ok(responses);
    }

    @Override
    public Result<List<StatisticResponse>> getDisplayedStatistics(GetDisplayedStatisticsRequest request) {
        log.info("Getting displayed statistics for userId='{}'", request.userId());
        List<Statistic> statistics = statisticRepository.findByUserId(request.userId()).stream()
            .filter(statistic -> statistic.getType().isDisplayed())
            .toList();
        List<StatisticResponse> responses = statistics.stream()
                .map(StatisticResponse::from)
                .toList();
        log.info("Retrieved {} displayed statistics for userId='{}'", responses.size(), request.userId());
        return Result.ok(responses);
    }

    @Override
    public Result<StatisticResponse> incrementStatistic(IncrementStatisticRequest request) {
        log.info("Incrementing statistic for userId='{}', type='{}', amount='{}'", request.userId(), request.type(), request.amount());
        Optional<Statistic> statisticOpt = statisticRepository.findByUserIdAndType(request.userId(), request.type());

        Statistic statistic;
        if (statisticOpt.isEmpty()) {
            log.debug("Statistic not found, creating new one for userId='{}', type='{}'", request.userId(), request.type());
            statistic = new Statistic(request.userId(), request.type());
            statistic.incrementAmount(request.amount());
            statisticRepository.create(statistic);
        } else {
            statistic = statisticOpt.get();
            statistic.incrementAmount(request.amount());
            statisticRepository.update(statistic);
        }

        log.info("Incremented statistic for userId='{}', type='{}', new value='{}'", request.userId(), request.type(), statistic.getAmount());
        return Result.ok(StatisticResponse.from(statistic));
    }

    @Override
    public Result<Void> initializeUserStatistics(@NonNull InitializeUserStatisticsRequest request) {
        log.info("Initializing statistics for userId='{}'", request.userId());
        for (StatisticType type : StatisticType.values()) {
            Optional<Statistic> existing = statisticRepository.findByUserIdAndType(request.userId(), type);
            if (existing.isEmpty()) {
                Statistic statistic = new Statistic(request.userId(), type);
                statisticRepository.create(statistic);
                log.debug("Created statistic for userId='{}', type='{}'", request.userId(), type);
            }
        }
        log.info("Statistics initialization completed for userId='{}'", request.userId());
        return Result.ok(null);
    }
}
