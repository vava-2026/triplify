package com.triplify.application.usecase.auth;

import com.google.inject.Inject;
import com.triplify.application.error.ValidationMapper;
import com.triplify.application.error.ValidationResult;
import com.triplify.application.result.Result;
import com.triplify.domain.error.ErrorCode;
import com.triplify.domain.model.User;
import com.triplify.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;

    @Inject
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Result<AuthResponse> login(LoginRequest command) {
        ValidationResult<LoginRequest> validation = ValidationMapper.validate(command);
        if (validation.isFailure()) {
            log.debug("Login validation failed: {}", validation.getViolations());
            return Result.failure(validation.getErrors());
        }

        return userRepository.findByUsernameOrEmail(command.username(), command.username())
                .map(user -> authenticate(user, command.password()))
                .orElseGet(() -> {
                    log.warn("Login attempt for unknown username='{}'", command.username());
                    return Result.failure(ErrorCode.AUTH_INVALID_CREDENTIALS);
                });
    }

    private Result<AuthResponse> authenticate(User user, String rawPassword) {
        if (!userRepository.verifyPassword(user.getId().toString(), rawPassword)) {
            log.warn("Invalid password attempt for username='{}'", user.getUsername());
            return Result.failure(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        log.info("User '{}' authenticated successfully", user.getUsername());
        return Result.success(new AuthResponse(user.getId().toString(), user.getUsername()));
    }
}
