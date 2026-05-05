package com.triplify.application.usecase.user.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.shared.DtoConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(min = 3, message = ValidationMessage.Constants.USERNAME_TOO_SHORT)
        @Size(max = DtoConstraints.USERNAME_MAX_LENGTH, message = ValidationMessage.Constants.USERNAME_TOO_LONG)
        String username

) {
}
