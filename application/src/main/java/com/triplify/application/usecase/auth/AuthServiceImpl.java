package com.triplify.application.usecase.auth;

import com.google.inject.Inject;
import com.triplify.domain.error.AuthError;
import com.triplify.domain.model.User;
import com.triplify.domain.repository.UserRepository;
import com.triplify.domain.result.Result;
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
        return findUser(command.username())
                .flatMap(user -> authenticate(user, command.password()))
                .map(user -> new AuthResponse(user.getId().toString(), user.getUsername()));
    }

    private Result<User> findUser(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(Result::ok)
                .orElseGet(() -> {
                    log.warn("Login attempt for unknown username='{}'", usernameOrEmail);
                    return Result.fail(new AuthError.InvalidCredentials());
                });
    }

    private Result<User> authenticate(User user, String rawPassword) {
        if (!userRepository.verifyPassword(user.getId().toString(), rawPassword)) {
            log.warn("Invalid password attempt for username='{}'", user.getUsername());
            return Result.fail(new AuthError.InvalidCredentials());
        }

        log.info("User '{}' authenticated successfully", user.getUsername());
        return Result.ok(user);
    }
}
