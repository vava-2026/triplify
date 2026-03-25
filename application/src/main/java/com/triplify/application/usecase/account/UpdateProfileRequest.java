package com.triplify.application.usecase.account;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// TODO: just for example of use of inputs, remove later
public record UpdateProfileRequest(

        @NotBlank(message = "{" + ValidationMessage.Constants.REQUIRED + "}")
        String name,

        @NotBlank(message = "{" + ValidationMessage.Constants.REQUIRED + "}")
        @Email(message = "{" + ValidationMessage.Constants.EMAIL_INVALID + "}")
        String email,

        @Size(min = 8, message = "{" + ValidationMessage.Constants.PASSWORD_TOO_SHORT + "}")
        String newPassword,

        @Size(max = 3000, message = "{" + ValidationMessage.Constants.DESCRIPTION_TOO_LONG + "}")
        String bio
) {}
