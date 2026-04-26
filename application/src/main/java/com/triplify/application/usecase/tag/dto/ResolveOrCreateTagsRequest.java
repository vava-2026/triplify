package com.triplify.application.usecase.tag.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;

public record ResolveOrCreateTagsRequest(
        @NotNull Collection<String> labels
) {
}
