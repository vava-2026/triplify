package com.triplify.domain.repository;

import com.triplify.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findAll();
    Optional<Category> findById(String id);
    boolean existsByName(String name, String nameSk);
    void create(Category category);
    void update(Category category);
    void delete(Category category);
}

