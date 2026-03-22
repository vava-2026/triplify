package com.triplify.application.usecase.auth;

import com.triplify.application.error.ErrorResponse;
import com.triplify.application.result.Result;

public interface AuthService {

    Result<AuthResponse, ErrorResponse> login(LoginRequest command);
}
