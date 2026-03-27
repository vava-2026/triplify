package com.triplify.application.usecase.auth.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record LogInRequest(
    @NotBlank(message = ValidationMessage.Constants.REQUIRED)
    @Email(message = ValidationMessage.Constants.EMAIL_INVALID)
    String email,

    @NotBlank(message = ValidationMessage.Constants.REQUIRED)
    @Size(min = 8, message = ValidationMessage.Constants.PASSWORD_TOO_SHORT)
    String password
) { }
