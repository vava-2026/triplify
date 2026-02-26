package com.triplify.domain.model.tag;

import java.util.UUID;

public record TripTag(
        UUID tripId,
        UUID tagId
) {
}
