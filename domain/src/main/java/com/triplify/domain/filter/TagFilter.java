package com.triplify.domain.filter;

import java.util.UUID;

public record TagFilter(
        UUID userId,
        String name
) {
    public TagFilter {
        if (userId == null) throw new IllegalArgumentException("userId must not be null");
        name = name == null ? null : name.trim();
    }
}
