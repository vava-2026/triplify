package com.triplify.domain.error;

public sealed interface TagError extends DomainError permits TagError.NotFound, TagError.AlreadyExists {

    record NotFound(String tagId) implements TagError {
        @Override
        public String code() {
            return "TAG_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Tag '%s' not found".formatted(tagId);
        }
    }

    record AlreadyExists(String name) implements TagError {
        @Override
        public String code() {
            return "TAG_ALREADY_EXISTS";
        }

        @Override
        public String message() {
            return "Tag '%s' already exists".formatted(name);
        }
    }
}

