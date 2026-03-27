package com.triplify.application.usecase.user;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.user.dto.UpdateUserAvatarRequest;
import com.triplify.application.usecase.user.dto.UpdateUserProfileRequest;
import com.triplify.application.usecase.user.dto.UserResponse;
import com.triplify.domain.result.Result;

public class UserServiceImpl implements UserService {

    @Override
    public Result<UserResponse> updateUserProfile(UpdateUserProfileRequest request) {
        // TODO: implement user profile update.
        return Result.fail(new ApplicationError.Unexpected("TODO: UserService.updateUserProfile"));
    }

    @Override
    public Result<UserResponse> updateUserAvatar(UpdateUserAvatarRequest request) {
        // TODO: implement user avatar update.
        return Result.fail(new ApplicationError.Unexpected("TODO: UserService.updateUserAvatar"));
    }

}
