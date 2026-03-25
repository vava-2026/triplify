package com.triplify.application.usecase.auth;

import com.triplify.domain.result.Result;

/**
 * Manages user authentication and session lifecycle
 */
public interface AuthService {

    /**
     * Authenticates a user using email and password. On success creates a session
     */
    Result<Void> login(LoginRequest request);

    /**
     * Registers a new user account and immediately authenticates (and creates a session).
     */
    Result<Void> signUp(SignUpRequest request);

    /**
     * Terminates the current user session. Safe to call if no session exists
     */
    void logout();
}
