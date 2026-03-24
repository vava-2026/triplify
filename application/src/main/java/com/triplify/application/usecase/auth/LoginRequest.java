package com.triplify.application.usecase.auth;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "{" + ValidationMessage.Constants.REQUIRED + "}")
        @Size(min = 3, message = "{" + ValidationMessage.Constants.USERNAME_TOO_SHORT + "}")
        String username,

        @NotBlank(message = "{" + ValidationMessage.Constants.REQUIRED + "}")
        @Size(min = 8, message = "{" + ValidationMessage.Constants.PASSWORD_TOO_SHORT + "}")
        String password
) {}
