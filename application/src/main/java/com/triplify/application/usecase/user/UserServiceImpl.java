package com.triplify.application.usecase.user;

import com.google.inject.Inject;
import com.triplify.application.shared.error.ApplicationError;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.user.dto.UpdateUserAvatarRequest;
import com.triplify.application.usecase.user.dto.UpdateUserProfileRequest;
import com.triplify.application.usecase.user.dto.UserResponse;
import com.triplify.domain.error.AuthError;
import com.triplify.domain.error.UserError;
import com.triplify.domain.model.User;
import com.triplify.domain.repository.UserRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final UserSessionContext sessionContext;

    @Inject
    public UserServiceImpl(UserRepository userRepository, ImageService imageService, UserSessionContext sessionContext) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.sessionContext = sessionContext;
    }

    @Override
    @Authenticated
    public Result<UserResponse> updateUserProfile(UpdateUserProfileRequest request) {
        SessionUser currentUser = sessionContext.getCurrent().orElseThrow();

        Optional<User> userOpt = userRepository.findByEmail(currentUser.email());
        if (userOpt.isEmpty()) {
            log.warn("Attempt to update profile for non-existing user with email='{}'", currentUser.email());
            return Result.fail(new UserError.NotFound(currentUser.userId().toString()));
        }

        User user = userOpt.get();
        String newUsername = request.username().trim();
        if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
            log.info("Attempt to update profile with taken username='{}'", newUsername);
            return Result.fail(new AuthError.UsernameAlreadyTaken());
        }

        user.updateUsername(newUsername);
        userRepository.update(user);

        log.info("Updated profile username for user id='{}'", user.getId());
        return Result.ok(UserResponse.from(user));
    }

    @Override
    public Result<UserResponse> updateUserAvatar(UpdateUserAvatarRequest request) {
        SessionUser currentUser = sessionContext.getCurrent().orElseThrow();

        Optional<User> userOpt = userRepository.findByEmail(currentUser.email());
        if (userOpt.isEmpty()) {
            log.warn("Attempt to update avatar for non-existing user with email='{}'", currentUser.email());
            return Result.fail(new ApplicationError.Unexpected("User not found"));
        }

        User user = userOpt.get();

        Result<ImageResponse> imageResult = imageService.addImage(new AddImageRequest(request.avatar(), "User avatar"));
        if (imageResult.isFailure()) {
            log.warn("Failed to store avatar image for user id='{}'", user.getId());
            return imageResult.map(ignored -> null);
        }

        ImageResponse image = imageResult.getValue();
        user.updateAvatar(image.id());
        userRepository.update(user);

        log.info("Updated avatar for user id='{}'", user.getId());
        return Result.ok(UserResponse.from(user));
    }
}
