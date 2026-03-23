package com.triplify.infrastructure.error;

import com.triplify.domain.error.AppError;

import java.nio.file.Path;

public sealed interface InfrastructureError extends AppError permits
        InfrastructureError.DatabaseError,
        InfrastructureError.FileError {

    record DatabaseError(String operation, Throwable cause) implements InfrastructureError {
        @Override
        public String code() {
            return "DB_ERROR";
        }

        @Override
        public String message() {
            return "Database error during: " + operation;
        }
    }

    record FileError(Path path, String reason) implements InfrastructureError {
        @Override
        public String code() {
            return "FILE_ERROR";
        }

        @Override
        public String message() {
            return "File error on '%s': %s".formatted(path, reason);
        }
    }
}

