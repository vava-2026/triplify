package com.triplify.application.usecase.user;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.user.dto.AddUserRequest;
import com.triplify.application.usecase.user.dto.DeleteUserRequest;
import com.triplify.application.usecase.user.dto.GetUserByIdRequest;
import com.triplify.application.usecase.user.dto.GetUsersRequest;
import com.triplify.application.usecase.user.dto.UpdateUserAvatarRequest;
import com.triplify.application.usecase.user.dto.UpdateUserProfileRequest;
import com.triplify.application.usecase.user.dto.UserResponse;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class UserServiceImpl implements UserService {

    @Override
    public Result<UserResponse> addUser(AddUserRequest request) {
        // TODO: implement user creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: UserService.addUser"));
    }

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

    @Override
    public Result<Void> deleteUser(DeleteUserRequest request) {
        // TODO: implement user deletion.
        return Result.fail(new ApplicationError.Unexpected("TODO: UserService.deleteUser"));
    }

    @Override
    public Result<UserResponse> getUserById(GetUserByIdRequest request) {
        // TODO: implement user retrieval by id.
        return Result.fail(new ApplicationError.Unexpected("TODO: UserService.getUserById"));
    }

    @Override
    public Result<Page<UserResponse>> getUsers(GetUsersRequest request) {
        // TODO: implement user search with pagination, name filter and creation order.
        return Result.fail(new ApplicationError.Unexpected("TODO: UserService.getUsers"));
    }
}

