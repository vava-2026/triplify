package com.triplify.domain.repository;

import com.triplify.domain.model.Tag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository {
    Optional<Tag> findById(String id);
    List<Tag> findByIds(Set<String> ids);
}
