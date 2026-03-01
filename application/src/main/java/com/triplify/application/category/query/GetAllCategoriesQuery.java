package com.triplify.application.category.query;

import an.awesome.pipelinr.Command;

import java.util.List;

public record GetAllCategoriesQuery() implements Command<List<CategoryResponse>> {
}

