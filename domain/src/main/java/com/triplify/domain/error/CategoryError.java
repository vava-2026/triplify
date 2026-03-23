package com.triplify.domain.error;

public sealed interface CategoryError extends DomainError permits CategoryError.NotFound, CategoryError.AlreadyExists {

    record NotFound(String categoryId) implements CategoryError {
        @Override
        public String code() {
            return "CATEGORY_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Category '%s' not found".formatted(categoryId);
        }
    }

    record AlreadyExists(String name) implements CategoryError {
        @Override
        public String code() {
            return "CATEGORY_ALREADY_EXISTS";
        }

        @Override
        public String message() {
            return "Category '%s' already exists".formatted(name);
        }
    }
}

