package com.triplify.application.usecase.image.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkImageRequest(
        @NotNull UUID imageId,
        @NotNull UUID ownerId,
        @NotNull com.triplify.domain.model.enums.ImageOwnerType ownerType,
        UUID tripId
) {}
