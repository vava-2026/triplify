package com.triplify.application.usecase.badge.dto;

public record GetBadgesRequest(
        Filter filter
) {

    public record Filter(
            String groupId,
            String createdById
    ) {

        public Filter {
            groupId = groupId == null ? null : groupId.trim();
            createdById = createdById == null ? null : createdById.trim();
        }
    }
}
