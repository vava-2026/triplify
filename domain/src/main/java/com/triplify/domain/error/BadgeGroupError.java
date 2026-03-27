package com.triplify.domain.error;

public sealed interface BadgeGroupError extends DomainError permits BadgeGroupError.NotFound {

    record NotFound(String groupId) implements BadgeGroupError {
        @Override
        public String code() {
            return "error.badge.group.not.found";
        }

        @Override
        public String message() {
            return "Badge group '%s' not found".formatted(groupId);
        }
    }
}

