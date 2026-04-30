package com.triplify.application.usecase.user;

import com.triplify.application.usecase.user.dto.UpdateUserAvatarRequest;
import com.triplify.application.usecase.user.dto.UpdateUserProfileRequest;
import com.triplify.application.usecase.user.dto.UserResponse;
import com.triplify.application.usecase.user.dto.InstallLicenseRequest;
import com.triplify.application.security.Authenticated;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.result.Result;

public interface UserService {

    Result<UserResponse> updateUserProfile(UpdateUserProfileRequest request);

    Result<UserResponse> updateUserAvatar(UpdateUserAvatarRequest request);

    @Authenticated(roles = {RoleEnum.USER, RoleEnum.PRO_USER})
    Result<Void> installLicense(InstallLicenseRequest request);
}
