package com.triplify.application.error;

import com.triplify.domain.error.AppError;

public sealed interface ApplicationError extends AppError permits
        ApplicationError.Unexpected,
        ApplicationError.StorageFailure,
        ApplicationError.FileFailure {

    record Unexpected(String detail) implements ApplicationError {
        @Override
        public String code() {
            return "error.application.unexpected";
        }

        @Override
        public String message() {
            return detail == null || detail.isBlank() ? "Unexpected application error" : detail;
        }
    }

    record StorageFailure(String operation, Throwable cause) implements ApplicationError {
        @Override
        public String code() {
            return "error.application.storage.failure";
        }

        @Override
        public String message() {
            return "Storage failure during operation '%s'".formatted(operation);
        }
    }

    record FileFailure(String operation, Throwable cause) implements ApplicationError {
        @Override
        public String code() {
            return "error.application.file.failure";
        }

        @Override
        public String message() {
            return "File failure during operation '%s'".formatted(operation);
        }
    }
}
