package com.triplify.application.usecase.image.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkImageRequest(
        @NotNull UUID imageId,
        @NotNull UUID ownerId,
        @NotNull ImageOwnerType ownerType,
        UUID tripId
) {}
