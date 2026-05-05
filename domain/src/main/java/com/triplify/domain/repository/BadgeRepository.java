package com.triplify.domain.repository;

import com.triplify.domain.model.Badge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BadgeRepository {
    List<Badge> findAll(UUID groupId, UUID createdById);
    Optional<Badge> findById(UUID id);
    boolean existsByNameAndLevel(UUID groupId, String name, int level);
    boolean existsByNameAndLevelExcludingId(UUID groupId, String name, int level, UUID excludedId);
    void create(Badge badge);
    void update(Badge badge);
    void delete(Badge badge);
}

