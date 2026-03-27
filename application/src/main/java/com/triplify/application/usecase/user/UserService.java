package com.triplify.application.usecase.user;

import com.triplify.application.usecase.user.dto.UpdateUserAvatarRequest;
import com.triplify.application.usecase.user.dto.UpdateUserProfileRequest;
import com.triplify.application.usecase.user.dto.UserResponse;
import com.triplify.domain.result.Result;

public interface UserService {

    Result<UserResponse> updateUserProfile(UpdateUserProfileRequest request);

    Result<UserResponse> updateUserAvatar(UpdateUserAvatarRequest request);

}
