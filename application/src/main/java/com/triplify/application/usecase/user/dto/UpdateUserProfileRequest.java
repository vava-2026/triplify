package com.triplify.application.usecase.user.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String userId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(min = 3, message = ValidationMessage.Constants.USERNAME_TOO_SHORT)
        String username,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Email(message = ValidationMessage.Constants.EMAIL_INVALID)
        String email
) {
}
