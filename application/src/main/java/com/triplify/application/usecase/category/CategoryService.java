package com.triplify.application.usecase.category;

import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.category.dto.CreateCategoryRequest;
import com.triplify.application.usecase.category.dto.DeleteCategoryRequest;
import com.triplify.application.usecase.category.dto.UpdateCategoryRequest;
import com.triplify.domain.result.Result;

import java.util.List;

public interface CategoryService {

    Result<CategoryResponse> createCategory(CreateCategoryRequest request);

    Result<CategoryResponse> updateCategory(UpdateCategoryRequest request);

    Result<Void> deleteCategory(DeleteCategoryRequest request);

    Result<List<CategoryResponse>> getAllCategories();
}
