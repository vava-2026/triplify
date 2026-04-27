package com.triplify.application.usecase.image.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.shared.DtoConstraints;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.nio.file.Path;

public record AddImageRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        Path image,

        @Size(max = DtoConstraints.DESCRIPTION_MAX_LENGTH, message = ValidationMessage.Constants.DESCRIPTION_TOO_LONG)
        String description
) {
}
