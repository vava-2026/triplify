package com.triplify.domain.error;

public sealed interface CategoryError extends DomainError permits CategoryError.NotFound, CategoryError.AlreadyExists {

    record NotFound(String categoryId) implements CategoryError {
        @Override
        public String code() {
            return "error.category.not.found";
        }

        @Override
        public String message() {
            return "Category '%s' not found".formatted(categoryId);
        }
    }

    record AlreadyExists(String name) implements CategoryError {
        @Override
        public String code() {
            return "error.category.already.exists";
        }

        @Override
        public String message() {
            return "Category '%s' already exists".formatted(name);
        }
    }
}

