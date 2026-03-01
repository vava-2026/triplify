package com.triplify.application.category.query;

import an.awesome.pipelinr.Command;
import com.triplify.domain.repository.CategoryRepository;
import jakarta.inject.Inject;

import java.util.List;

public class GetAllCategoriesQueryHandler implements Command.Handler<GetAllCategoriesQuery, List<CategoryResponse>> {

    private final CategoryRepository categoryRepository;

    @Inject
    public GetAllCategoriesQueryHandler(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> handle(GetAllCategoriesQuery query) {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.id(), c.name()))
                .toList();
    }
}

