package com.triplify.domain.repository;

import com.triplify.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Optional<Category> findById(UUID id);
    List<Category> findAll();
    boolean existsByName(String name, String nameSk);
    void create(Category category);
    void update(Category category);
    void delete(Category category);
}

