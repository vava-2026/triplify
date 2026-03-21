package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Category;
import com.triplify.domain.repository.CategoryRepository;

import java.util.List;

public class CategoryRepositoryImpl implements CategoryRepository {

    private static final List<Category> CATEGORIES = List.of(
            new Category("1", "Adventure"),
            new Category("2", "Beach"),
            new Category("3", "City Break"),
            new Category("4", "Cultural"),
            new Category("5", "Nature")
    );

    @Override
    public List<Category> findAll() {
        return CATEGORIES;
    }
}

