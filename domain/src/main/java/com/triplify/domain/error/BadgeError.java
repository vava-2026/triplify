package com.triplify.domain.error;

public sealed interface BadgeError extends DomainError permits BadgeError.NotFound, BadgeError.AlreadyExists {

    record NotFound(String badgeId) implements BadgeError {
        @Override
        public String code() {
            return "error.badge.not.found";
        }

        @Override
        public String message() {
            return "Badge '%s' not found".formatted(badgeId);
        }
    }

    record AlreadyExists(String name) implements BadgeError {
        @Override
        public String code() {
            return "error.badge.already.exists";
        }

        @Override
        public String message() {
            return "Badge '%s' already exists".formatted(name);
        }
    }
}

