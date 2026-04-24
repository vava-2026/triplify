package com.triplify.application.usecase.trip.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.usecase.dto.DtoConstraints;
import com.triplify.domain.model.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UpdateTripRequest(

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID id,

        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        UUID categoryId,

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        @Size(max = DtoConstraints.TITLE_MAX_LENGTH, message = ValidationMessage.Constants.TITLE_TOO_LONG)
        String title,

        @Size(max = DtoConstraints.DESCRIPTION_MAX_LENGTH, message = ValidationMessage.Constants.DESCRIPTION_TOO_LONG)
        String description,
        @NotNull(message = ValidationMessage.Constants.REQUIRED)
        StatusEnum status,
        Instant startedAt,
        Instant endedAt,
        Set<UUID> tagIds,
        Set<Path> images,
        Set<UUID> countryIds
) {
}
