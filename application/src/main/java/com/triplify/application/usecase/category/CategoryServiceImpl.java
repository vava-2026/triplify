package com.triplify.application.usecase.category;

import com.google.inject.Inject;
import com.triplify.domain.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;

    @Inject
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.debug("Getting all categories");
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId().toString(), c.getName()))
                .toList();
    }
}

