package com.triplify.application.category.query;

import an.awesome.pipelinr.Command;
import com.triplify.domain.repository.CategoryRepository;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GetAllCategoriesQueryHandler implements Command.Handler<GetAllCategoriesQuery, List<CategoryResponse>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllCategoriesQueryHandler.class);
    private final CategoryRepository categoryRepository;

    @Inject
    public GetAllCategoriesQueryHandler(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> handle(GetAllCategoriesQuery query) {
        log.debug("Getting all categories");
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.id(), c.name()))
                .toList();
    }
}

