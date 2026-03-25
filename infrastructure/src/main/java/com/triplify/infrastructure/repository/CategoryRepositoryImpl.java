package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Category;
import com.triplify.domain.model.enums.ColorEnum;
import com.triplify.domain.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryRepositoryImpl implements CategoryRepository {
    private static List<Category> CATEGORIES = new ArrayList<>();

    public void initializeCategories(UUID configurationManagerId) {
        CATEGORIES.add(new Category(configurationManagerId,"Adventure", "Advanture_SK", ColorEnum.GREEN));
        CATEGORIES.add(new Category(configurationManagerId,"Beach", "Beach_SK", ColorEnum.RED));
        CATEGORIES.add(new Category(configurationManagerId,"City Break", "City Break_SK", ColorEnum.BLUE));
        CATEGORIES.add(new Category(configurationManagerId,"Cultural", "Cultural_SK", ColorEnum.ORANGE));
        CATEGORIES.add(new Category(configurationManagerId,"Nature", "Nature_SK", ColorEnum.TEAL));
    }

    @Override
    public void save(Category category) {
        CategoryRepositoryImpl.CATEGORIES.add(category);
    }

    @Override
    public List<Category> findAll() {
        return CATEGORIES;
    }
}

