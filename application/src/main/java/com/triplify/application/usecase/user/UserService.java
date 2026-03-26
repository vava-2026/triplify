package com.triplify.application.usecase.user;

import com.triplify.application.usecase.user.dto.AddUserRequest;
import com.triplify.application.usecase.user.dto.DeleteUserRequest;
import com.triplify.application.usecase.user.dto.GetUserByIdRequest;
import com.triplify.application.usecase.user.dto.GetUsersRequest;
import com.triplify.application.usecase.user.dto.UpdateUserAvatarRequest;
import com.triplify.application.usecase.user.dto.UpdateUserProfileRequest;
import com.triplify.application.usecase.user.dto.UserResponse;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public interface UserService {

    Result<UserResponse> addUser(AddUserRequest request);

    Result<UserResponse> updateUserProfile(UpdateUserProfileRequest request);

    Result<UserResponse> updateUserAvatar(UpdateUserAvatarRequest request);

    Result<Void> deleteUser(DeleteUserRequest request);

    Result<UserResponse> getUserById(GetUserByIdRequest request);

    Result<Page<UserResponse>> getUsers(GetUsersRequest request);
}
