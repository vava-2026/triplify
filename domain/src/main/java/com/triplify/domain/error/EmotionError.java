package com.triplify.domain.error;

import java.util.UUID;

public sealed interface EmotionError extends DomainError permits EmotionError.NotFound, EmotionError.AlreadyExists {

    record NotFound(UUID emotionId) implements EmotionError {
        @Override
        public String code() {
            return "error.emotion.not.found";
        }

        @Override
        public String message() {
            return "Emotion '%s' not found".formatted(emotionId.toString());
        }
    }

    record AlreadyExists(String name) implements EmotionError {
        @Override
        public String code() {
            return "error.emotion.already.exists";
        }

        @Override
        public String message() {
            return "Emotion '%s' already exists".formatted(name);
        }
    }
}

