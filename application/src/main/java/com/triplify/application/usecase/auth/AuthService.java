package com.triplify.application.usecase.auth;

import com.triplify.domain.result.Result;

/**
 * Manages user authentication and session lifecycle
 */
public interface AuthService {

    /**
     * Authenticates a user using email and password. On success creates a session
     *
     * @return Possible errors:
     * {@link com.triplify.domain.error.AuthError.InvalidCredentials}
     */
    Result<Void> login(LoginRequest request);

    /**
     * Registers a new user account and immediately authenticates (and creates a session).
     *
     * @return Possible errors:
     * {@link com.triplify.domain.error.AuthError.UsernameAlreadyTaken},
     * {@link com.triplify.domain.error.AuthError.EmailAlreadyTaken}
     */
    Result<Void> signUp(SignUpRequest request);

    /**
     * Terminates the current user session. Safe to call if no session exists
     */
    void logout();
}
