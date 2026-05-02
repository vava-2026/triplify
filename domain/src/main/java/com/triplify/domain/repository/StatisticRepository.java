package com.triplify.domain.repository;

import com.triplify.domain.model.Statistic;
import com.triplify.domain.model.enums.StatisticType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatisticRepository {
    Optional<Statistic> findById(UUID id);
    Optional<Statistic> findByUserIdAndType(UUID userId, StatisticType type);
    List<Statistic> findByUserId(UUID userId);
    void create(Statistic statistic);
    void update(Statistic statistic);
    void delete(UUID id);
}
