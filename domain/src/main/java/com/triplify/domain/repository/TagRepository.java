package com.triplify.domain.repository;

import com.triplify.domain.filter.TagFilter;
import com.triplify.domain.model.Tag;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.Optional;

public interface TagRepository {
    Page<Tag> findList(PageRequest pageRequest, TagFilter filter);
    Optional<Tag> findById(String id);
    boolean existsByUserIdAndName(String userId, String name);
    void create(Tag tag);
    void update(Tag tag);
    void delete(Tag tag);
}
