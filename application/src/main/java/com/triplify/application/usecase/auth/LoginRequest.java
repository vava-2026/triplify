package com.triplify.application.usecase.auth;

import com.triplify.application.error.ValidationMessage;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


public record LoginRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(min = 3, message = ValidationMessage.Constants.USERNAME_TO_SHORT)
        String username,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(min = 8, message = ValidationMessage.Constants.PASSWORD_TO_SHORT)
        String password
) {}
