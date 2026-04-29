package com.triplify.domain.filter;

import com.triplify.domain.model.enums.StatusEnum;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record TripFilter(
        String name,
        String countryId,
        StatusEnum status,
        String categoryId,
        Set<UUID> tagIds,
        Instant startedFrom,
        Instant startedTo
) {
}
