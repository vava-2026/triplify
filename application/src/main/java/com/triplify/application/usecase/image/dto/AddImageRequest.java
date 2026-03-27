package com.triplify.application.usecase.image.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddImageRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String url,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String storageKey,

        @Size(max = DtoConstraints.DESCRIPTION_MAX_LENGTH, message = ValidationMessage.Constants.DESCRIPTION_TOO_LONG)
        String description,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String ownerId,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        ImageOwnerType ownerType
) {

    public AddImageRequest {
        ownerId = ownerId == null ? null : ownerId.trim();
    }
}
