package com.triplify.application.usecase.user.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.domain.model.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddUserRequest(

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
}
