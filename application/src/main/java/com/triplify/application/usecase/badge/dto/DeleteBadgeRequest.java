package com.triplify.application.usecase.badge.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeleteBadgeRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String badgeId
) {
}
