package com.triplify.application.usecase.auth;

import com.triplify.domain.result.Result;

public interface AuthService {

    Result<Void> login(LoginRequest request);
    Result<Void> signUp(SignUpRequest request);
    void logout();
}
