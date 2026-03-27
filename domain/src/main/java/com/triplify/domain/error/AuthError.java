package com.triplify.domain.error;

public sealed interface AuthError extends DomainError permits
        AuthError.EmailAlreadyTaken,
        AuthError.InvalidCredentials,
        AuthError.SessionExpired,
        AuthError.UsernameAlreadyTaken {

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

    record UsernameAlreadyTaken() implements AuthError {
        @Override
        public String code() {
            return "error.auth.username.already.taken";
        }

        @Override
        public String message() {
            return "Username is already taken";
        }
    }

    record EmailAlreadyTaken() implements AuthError {
        @Override
        public String code() {
            return "error.auth.email.already.taken";
        }

        @Override
        public String message() {
            return "Email is already taken";
        }
    }
}

