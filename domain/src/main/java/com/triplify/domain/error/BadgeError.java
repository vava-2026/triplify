package com.triplify.domain.error;

public sealed interface BadgeError extends DomainError permits BadgeError.NotFound, BadgeError.AlreadyExists {

    record NotFound(String badgeId) implements BadgeError {
        @Override
        public String code() {
            return "BADGE_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Badge '%s' not found".formatted(badgeId);
        }
    }

    record AlreadyExists(String name) implements BadgeError {
        @Override
        public String code() {
            return "BADGE_ALREADY_EXISTS";
        }

        @Override
        public String message() {
            return "Badge '%s' already exists".formatted(name);
        }
    }
}

