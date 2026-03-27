package com.triplify.application.usecase.category;

import com.triplify.domain.result.Result;

import java.util.List;

public interface CategoryService {
    Result<List<CategoryResponse>> getAllCategories();
}
