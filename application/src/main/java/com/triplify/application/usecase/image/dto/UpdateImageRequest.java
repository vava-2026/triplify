package com.triplify.application.usecase.image.dto;

import com.triplify.application.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.file.Path;

public record UpdateImageRequest(
        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String imageId,

        Path image,

        @Size(max = DtoConstraints.DESCRIPTION_MAX_LENGTH, message = ValidationMessage.Constants.DESCRIPTION_TOO_LONG)
        String description
) {
}
