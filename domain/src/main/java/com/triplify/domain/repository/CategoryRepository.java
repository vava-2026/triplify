package com.triplify.domain.repository;

import com.triplify.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(String id);
    List<Category> findAll();
    void save(Category category);
}

