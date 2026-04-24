package com.triplify.domain.repository;

import com.triplify.domain.filter.StoryFilter;
import com.triplify.domain.model.Story;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.Optional;
import java.util.UUID;

public interface StoryRepository {
    Optional<Story> findById(UUID id);
    Page<Story> findList(PageRequest pageRequest, StoryFilter filter);
    void create(Story story);
    void update(Story story);
    void delete(Story story);
}
