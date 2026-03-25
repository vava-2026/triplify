package com.triplify.domain.error;

public sealed interface AuthError extends DomainError permits AuthError.InvalidCredentials, AuthError.SessionExpired {

    record InvalidCredentials() implements AuthError {
        @Override
        public String code() {
            return "error.auth.invalid.credentials";
        }

        @Override
        public String message() {
            return "Invalid username or password";
        }
    }

    record SessionExpired() implements AuthError {
        @Override
        public String code() {
            return "error.auth.session.expired";
        }

        @Override
        public String message() {
            return "Session has expired, please log in again";
        }
    }
}

