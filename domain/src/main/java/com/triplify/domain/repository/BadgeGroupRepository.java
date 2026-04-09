package com.triplify.domain.repository;

import com.triplify.domain.model.BadgeGroup;

import java.util.List;
import java.util.Optional;

public interface BadgeGroupRepository {
    List<BadgeGroup> findAll();
    Optional<BadgeGroup> findById(String id);
}

