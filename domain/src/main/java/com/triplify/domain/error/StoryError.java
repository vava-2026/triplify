package com.triplify.domain.error;

public sealed interface StoryError extends DomainError permits
        StoryError.NotFound,
        StoryError.NotOwner,
        StoryError.PremiumRequired,
        StoryError.InvalidStatusTransition {

    record NotFound(String storyId) implements StoryError {
        @Override
        public String code() {
            return "STORY_NOT_FOUND";
        }

        @Override
        public String message() {
            return "Story '%s' not found".formatted(storyId);
        }
    }

    record NotOwner(String storyId) implements StoryError {
        @Override
        public String code() {
            return "STORY_NOT_OWNER";
        }

        @Override
        public String message() {
            return "You are not the owner of story '%s'".formatted(storyId);
        }
    }

    record PremiumRequired() implements StoryError {
        @Override
        public String code() {
            return "STORY_PREMIUM_REQUIRED";
        }

        @Override
        public String message() {
            return "Story functionality requires a premium account";
        }
    }

    record InvalidStatusTransition(String from, String to) implements StoryError {
        @Override
        public String code() {
            return "STORY_INVALID_STATUS_TRANSITION";
        }

        @Override
        public String message() {
            return "Cannot transition story from '%s' to '%s'".formatted(from, to);
        }
    }
}

