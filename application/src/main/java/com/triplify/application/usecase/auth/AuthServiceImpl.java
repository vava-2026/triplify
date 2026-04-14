package com.triplify.application.usecase.auth;

import com.google.inject.Inject;
import com.triplify.application.usecase.auth.dto.LogInRequest;
import com.triplify.application.usecase.auth.dto.SignUpRequest;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.error.AuthError;
import com.triplify.domain.model.User;
import com.triplify.domain.repository.UserRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final com.triplify.domain.service.PasswordEncoder passwordEncoder;
    private final UserSessionContext sessionContext;

    @Inject
    public AuthServiceImpl(UserRepository userRepository, com.triplify.domain.service.PasswordEncoder passwordEncoder, UserSessionContext sessionContext) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionContext = sessionContext;
    }

    @Override
    public Result<Void> login(LogInRequest request) {
        return userRepository.findByEmail(request.email())
                .map(user -> authenticate(user, request.password()))
                .orElseGet(() -> {
                    log.warn("Login attempt for unknown email='{}'", request.email());
                    return Result.fail(new AuthError.InvalidCredentials());
                });
    }

    @Override
    public Result<Void> signUp(SignUpRequest command) {
        if (userRepository.existsByUsername(command.username())) {
            log.info("SignUp, username already exists='{}'", command.username());
            return Result.fail(new AuthError.UsernameAlreadyTaken());
        }

        if (userRepository.existsByEmail(command.email())) {
            log.info("SignUp, email already taken='{}'", command.email());
            return Result.fail(new AuthError.EmailAlreadyTaken());
        }

        String passwordHash = passwordEncoder.encode(command.password());
        User user = new User(command.username(), command.email(), passwordHash, command.role());
        userRepository.save(user);

        log.info("User '{}' registered successfully", user.getUsername());
        sessionContext.set(new SessionUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarImageId()));
        sessionContext.save();
        return Result.ok();
    }

    @Override
    public void logout() {
        sessionContext.clear();
    }

    private Result<Void> authenticate(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.info("Invalid password attempt for username='{}'", user.getUsername());
            return Result.fail(new AuthError.InvalidCredentials());
        }

        log.info("User '{}' authenticated successfully", user.getUsername());
        sessionContext.set(new SessionUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarImageId()));
        sessionContext.save();
        return Result.ok();
    }
}
