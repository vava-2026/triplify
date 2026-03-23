package com.triplify.application.usecase.auth;

import com.triplify.domain.result.Result;

public interface AuthService {

    Result<AuthResponse> login(LoginRequest command);
}
