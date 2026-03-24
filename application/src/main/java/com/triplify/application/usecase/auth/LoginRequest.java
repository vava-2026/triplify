package com.triplify.application.usecase.auth;

import com.triplify.application.error.ValidationMessage;
import com.triplify.domain.model.enums.RoleEnum;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record LoginRequest(
    @NotBlank(message = ValidationMessage.Constants.REQUIRED)
    @Email(message = ValidationMessage.Constants.EMAIL_INVALID)
    String email,

    @NotBlank(message = ValidationMessage.Constants.REQUIRED)
    @Size(min = 8, message = ValidationMessage.Constants.PASSWORD_TOO_SHORT)
    String password
) { }
