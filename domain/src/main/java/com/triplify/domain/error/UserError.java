package com.triplify.domain.error;

public sealed interface UserError extends DomainError permits
        UserError.NotFound,
        UserError.AlreadyExists,
        UserError.Unauthorized,
        UserError.Forbidden,
        UserError.InvalidCurrentPassword {

    record NotFound(String userId) implements UserError {
        @Override
        public String code() {
            return "error.user.not.found";
        }

        @Override
        public String message() {
            return "User '%s' not found".formatted(userId);
        }
    }

    record AlreadyExists(String identity) implements UserError {
        @Override
        public String code() {
            return "error.user.already.exists";
        }

        @Override
        public String message() {
            return "User '%s' already exists".formatted(identity);
        }
    }

    record Unauthorized() implements UserError {
        @Override
        public String code() {
            return "error.user.unauthorized";
        }

        @Override
        public String message() {
            return "You must be logged in to perform this action";
        }
    }

    record Forbidden() implements UserError {
        @Override
        public String code() {
            return "error.user.forbidden";
        }

        @Override
        public String message() {
            return "You do not have permission to perform this action";
        }
    }

    record InvalidCurrentPassword() implements UserError {
        @Override
        public String code() {
            return "error.user.invalid.current.password";
        }

        @Override
        public String message() {
            return "Current password is incorrect";
        }
    }
}

