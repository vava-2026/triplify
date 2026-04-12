package com.triplify.domain.repository;

import com.triplify.domain.model.Badge;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository {
    List<Badge> findAll(String groupId, String createdById);
    Optional<Badge> findById(String id);
    boolean existsByNameAndLevel(String groupId, String name, int level);
    boolean existsByNameAndLevelExcludingId(String groupId, String name, int level, String excludedId);
    void create(Badge badge);
    void update(Badge badge);
    void delete(Badge badge);
}

