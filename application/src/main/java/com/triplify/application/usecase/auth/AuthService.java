package com.triplify.application.usecase.auth;

import com.triplify.application.result.Result;

public interface AuthService {

    Result<Void> login(LoginRequest request);
    Result<Void> signUp(SignUpRequest request);
    void logout();
}
