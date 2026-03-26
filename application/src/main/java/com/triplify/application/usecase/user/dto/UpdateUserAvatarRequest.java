package com.triplify.application.usecase.user.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserAvatarRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String userId,

        String avatarImageId
) {
}
