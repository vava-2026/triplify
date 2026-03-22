package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Category;
import com.triplify.domain.repository.CategoryRepository;

import java.util.List;
import java.util.UUID;

public class CategoryRepositoryImpl implements CategoryRepository {

    private static final List<Category> CATEGORIES = List.of(
            Category.builder().createdById(UUID.randomUUID()).name("Adventure").nameSk("Adventure_sk").build(),
            Category.builder().createdById(UUID.randomUUID()).name("Beach").nameSk("Beach_sk").build(),
            Category.builder().createdById(UUID.randomUUID()).name("City Break").nameSk("City Break SK").build(),
            Category.builder().createdById(UUID.randomUUID()).name("Cultural").nameSk("Cultural SK").build(),
            Category.builder().createdById(UUID.randomUUID()).name("Nature").nameSk("Nature SK").build()
    );

    @Override
    public List<Category> findAll() {
        return CATEGORIES;
    }
}

