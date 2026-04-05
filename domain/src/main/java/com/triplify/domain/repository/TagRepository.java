package com.triplify.domain.repository;

import com.triplify.domain.model.Tag;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository {
    Optional<Tag> findById(String id);
    Optional<Tag> findByUserIdAndName(String userId, String name);
    List<Tag> findByIds(Set<String> ids);
    Page<Tag> findList(PageRequest pageRequest, String name);
    void create(Tag tag);
}
