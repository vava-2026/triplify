package com.triplify.application.usecase.category;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.model.ColorTheme;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.category.dto.AddCategoryRequest;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.category.dto.DeleteCategoryRequest;
import com.triplify.application.usecase.category.dto.UpdateCategoryRequest;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.error.CategoryError;
import com.triplify.domain.model.Category;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.repository.CategoryRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Authenticated
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;
    private final UserSessionContext sessionContext;

    @Inject
    public CategoryServiceImpl(CategoryRepository categoryRepository, UserSessionContext sessionContext) {
        this.categoryRepository = categoryRepository;
        this.sessionContext = sessionContext;
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<CategoryResponse> addCategory(AddCategoryRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        if (categoryRepository.existsByName(request.name(), request.nameSk())) {
            log.warn("Attempted to add category with existing name='{}' or nameSk='{}' by userId='{}'",
                    request.name(), request.nameSk(), user.userId());
            return Result.fail(new CategoryError.AlreadyExists(request.name()));
        }

        Category category = new Category(user.userId(), request.name(), request.nameSk(), request.color().toColorEnum());
        category.updateDescription(request.description());
        category.updateDescriptionSk(request.descriptionSk());
        category.updateEmojiUnicode(request.emojiUnicode());

        categoryRepository.create(category);
        log.info("Added new category with id='{}', name='{}' by userId='{}'",
                category.getId(), category.getName(), user.userId());
        return Result.ok(toResponse(category));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<CategoryResponse> updateCategory(UpdateCategoryRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Category> existing = categoryRepository.findById(request.id());
        if (existing.isEmpty()) {
            log.warn("Attempt to update non-existing category with id='{}' by userId='{}'",
                    request.id(), user.userId());
            return Result.fail(new CategoryError.NotFound(request.id().toString()));
        }

        Category category = existing.get();

        try {
            category.updateName(request.name());
            category.updateNameSk(request.nameSk());
            category.updateDescription(request.description());
            category.updateDescriptionSk(request.descriptionSk());
            category.updateEmojiUnicode(request.emojiUnicode());
            category.updateColor(request.color().toColorEnum());
        } catch (IllegalArgumentException ex) {
            return Result.fail(new ApplicationError.Unexpected(ex.getMessage()));
        }

        categoryRepository.update(category);
        log.info("Updated category with id='{}', name='{}' by userId='{}'",
                category.getId(), category.getName(), user.userId());
        return Result.ok(toResponse(category));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<Void> deleteCategory(DeleteCategoryRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Category> existing = categoryRepository.findById(request.id());
        if (existing.isEmpty()) {
            log.warn("Attempt to delete non-existing category with id='{}' by userId='{}'",
                    request.id(), user.userId());
            return Result.fail(new CategoryError.NotFound(request.id().toString()));
        }

        categoryRepository.delete(existing.get());
        log.info("Deleted category with id='{}' by userId='{}'", request.id(), user.userId());
        return Result.ok(null);
    }

    @Override
    public Result<List<CategoryResponse>> getAllCategories() {
        log.debug("Getting all categories");
        try {
            List<CategoryResponse> categories = categoryRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
            return Result.ok(categories);
        } catch (Exception ex) {
            return Result.fail(new ApplicationError.StorageFailure("getAllCategories", ex));
        }
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getCreatedById(),
                c.getName(),
                c.getNameSk(),
                c.getDescription(),
                c.getDescriptionSk(),
                c.getEmojiUnicode(),
               ColorTheme.from(c.getColor())
        );
    }
}
