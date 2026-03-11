package com.triplify.domain.repository;

import com.triplify.domain.model.Category;

import java.util.List;

public interface CategoryRepository {
    List<Category> findAll();
}

