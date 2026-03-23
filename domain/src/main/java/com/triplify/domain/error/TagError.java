package com.triplify.domain.error;

public sealed interface TagError extends DomainError permits TagError.NotFound, TagError.AlreadyExists {

    record NotFound(String tagId) implements TagError {
        @Override
        public String code() {
            return "error.tag.not.found";
        }

        @Override
        public String message() {
            return "Tag '%s' not found".formatted(tagId);
        }
    }

    record AlreadyExists(String name) implements TagError {
        @Override
        public String code() {
            return "error.tag.already.exists";
        }

        @Override
        public String message() {
            return "Tag '%s' already exists".formatted(name);
        }
    }
}

