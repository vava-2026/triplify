package com.triplify.domain.error;

public sealed interface AuthError extends DomainError permits AuthError.InvalidCredentials, AuthError.SessionExpired {

    record InvalidCredentials() implements AuthError {
        @Override
        public String code() {
            return "AUTH_INVALID_CREDENTIALS";
        }

        @Override
        public String message() {
            return "Invalid username or password";
        }
    }

    record SessionExpired() implements AuthError {
        @Override
        public String code() {
            return "AUTH_SESSION_EXPIRED";
        }

        @Override
        public String message() {
            return "Session has expired, please log in again";
        }
    }
}

