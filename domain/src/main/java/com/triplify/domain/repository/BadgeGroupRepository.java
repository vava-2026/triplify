package com.triplify.domain.repository;

import com.triplify.domain.model.BadgeGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BadgeGroupRepository {
    List<BadgeGroup> findAll();
    Optional<BadgeGroup> findById(UUID id);
}

