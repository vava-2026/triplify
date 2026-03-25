package com.triplify.application.usecase.category;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.model.ColorTheme;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.category.dto.CreateCategoryRequest;
import com.triplify.application.usecase.category.dto.DeleteCategoryRequest;
import com.triplify.application.usecase.category.dto.UpdateCategoryRequest;
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
    public Result<CategoryResponse> createCategory(CreateCategoryRequest request) {
        // TODO: implement category creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: CategoryService.createCategory"));
    }

    @Override
    public Result<CategoryResponse> updateCategory(UpdateCategoryRequest request) {
        // TODO: implement category update.
        return Result.fail(new ApplicationError.Unexpected("TODO: CategoryService.updateCategory"));
    }

    @Override
    public Result<Void> deleteCategory(DeleteCategoryRequest request) {
        // TODO: implement category delete.
        return Result.fail(new ApplicationError.Unexpected("TODO: CategoryService.deleteCategory"));
    }

    @Override
    public Result<List<CategoryResponse>> getAllCategories() {
        log.debug("Getting all categories");
        try {
            List<com.triplify.application.usecase.category.dto.CategoryResponse> categories = categoryRepository.findAll().stream()
                    .map(c -> new com.triplify.application.usecase.category.dto.CategoryResponse(
                            c.getId().toString(),
                            c.getCreatedById().toString(),
                            c.getName(),
                            c.getNameSk(),
                            c.getDescription(),
                            c.getDescriptionSk(),
                            c.getEmojiUnicode(),
                            c.getColor() == null ? null : ColorTheme.from(c.getColor())
                    ))
                    .toList();
            return Result.ok(categories);
        } catch (Exception ex) {
            return Result.fail(new ApplicationError.StorageFailure("getAllCategories", ex));
        }
    }
}
