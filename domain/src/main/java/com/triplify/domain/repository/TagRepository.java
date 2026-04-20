package com.triplify.domain.repository;

import com.triplify.domain.model.Tag;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository {
    Optional<Tag> findById(UUID id);
    Optional<Tag> findByUserIdAndName(UUID userId, String name);
    List<Tag> findByIds(Set<UUID> ids);
    Page<Tag> findList(PageRequest pageRequest, String name);
    void create(Tag tag);
}
