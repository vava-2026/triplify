package com.triplify.application.usecase.badge.dto;

import java.util.UUID;

public record GetBadgesRequest(
        Filter filter
) {

    public record Filter(
            UUID groupId,
            UUID createdById
    ) {
    }
}
