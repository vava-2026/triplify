package com.triplify.domain.error;

public sealed interface BadgeGroupError extends DomainError permits BadgeGroupError.NotFound {

    record NotFound(String groupId) implements BadgeGroupError {
        @Override
        public String code() {
            return "BADGE_GROUP_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Badge group '%s' not found".formatted(groupId);
        }
    }
}

