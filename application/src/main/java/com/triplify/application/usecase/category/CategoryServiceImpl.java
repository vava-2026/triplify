package com.triplify.application.usecase.category;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.domain.repository.CategoryRepository;
import com.triplify.domain.result.Result;
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
    public Result<List<CategoryResponse>> getAllCategories() {
        log.debug("Getting all categories");
        try {
            List<CategoryResponse> categories = categoryRepository.findAll().stream()
                    .map(c -> new CategoryResponse(c.getId().toString(), c.getName()))
                    .toList();
            return Result.ok(categories);
        } catch (Exception ex) {
            return Result.fail(new ApplicationError.StorageFailure("getAllCategories", ex));
        }
    }
}
