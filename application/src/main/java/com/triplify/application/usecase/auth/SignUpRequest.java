package com.triplify.application.usecase.auth;

import com.triplify.application.error.ValidationMessage;
import com.triplify.domain.model.enums.RoleEnum;
import jakarta.validation.constraints.*;

public record SignUpRequest(
    @NotBlank(message = ValidationMessage.Constants.REQUIRED)
    @Size(min = 3, message = ValidationMessage.Constants.USERNAME_TOO_SHORT)
    String username,

    @NotBlank(message = ValidationMessage.Constants.REQUIRED)
    @Email(message = ValidationMessage.Constants.EMAIL_INVALID)
    String email,

    @NotBlank(message = ValidationMessage.Constants.REQUIRED)
    @Size(min = 8, message = ValidationMessage.Constants.PASSWORD_TOO_SHORT)
    String password,

    @NotNull(message = ValidationMessage.Constants.REQUIRED)
    RoleEnum role
) {
    @AssertTrue(message = ValidationMessage.Constants.SIGN_UP_INVALID_ROLE)
    public boolean isValidRole() {
        if (role == null) {
            return true;
        }
        return role == RoleEnum.CONFIGURATION_MANAGER || role == RoleEnum.USER;
    }
}
