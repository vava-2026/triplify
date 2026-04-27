package com.triplify.ui.shared.component.categories.model;

import com.triplify.application.usecase.category.CategoryService;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.domain.result.Result;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.util.Localization;

import java.util.ArrayList;
import java.util.List;

public class Categories {

    private final CategoryService categoryService;

    private Categories(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public Result<List<CategoryResponse>> loadAll() {
        if (categoryService == null) {
            return Result.ok(List.of());
        }
        return categoryService.getAllCategories();
    }

    public List<Entry<String>> toEntries(List<CategoryResponse> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        List<Entry<String>> entries = new ArrayList<>();
        for (CategoryResponse category : categories) {
            if (category == null || category.id() == null || category.name() == null || category.name().isBlank()) {
                continue;
            }
            entries.add(Entry.<String>builder(category.id().toString(), Localization.localize(category))
                    .emoji(category.emojiUnicode())
                    .build());
        }
        return entries;
    }

    public List<Entry<String>> toEntriesWithAll(List<CategoryResponse> categories, String allLabel) {
        List<Entry<String>> entries = new ArrayList<>();
        entries.add(Entry.builder("", allLabel == null || allLabel.isBlank() ? "All" : allLabel).build());
        entries.addAll(toEntries(categories));
        return entries;
    }

    public static Builder builder(CategoryService categoryService) {
        return new Builder(categoryService);
    }

    public static final class Builder {
        private final CategoryService categoryService;

        private Builder(CategoryService categoryService) {
            this.categoryService = categoryService;
        }

        public Categories build() {
            return new Categories(categoryService);
        }
    }
}

