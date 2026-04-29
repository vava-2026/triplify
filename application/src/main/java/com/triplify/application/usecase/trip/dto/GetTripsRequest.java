package com.triplify.application.usecase.trip.dto;

import com.triplify.application.shared.error.ValidationMessage;
import com.triplify.application.shared.DtoConstraints;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.PageRequest;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record GetTripsRequest(
        PageRequest pageRequest,
        Filter filter,
        OrderBy orderBy
) {

    public GetTripsRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
        filter = filter == null ? new Filter(null, null, null, null, null, null, null) : filter;
    }

    public record Filter(
            @Size(max = DtoConstraints.NAME_MAX_LENGTH, message = ValidationMessage.Constants.NAME_TOO_LONG)
            String name,
            String countryId,
            StatusEnum status,
            String categoryId,
            Set<UUID> tagIds,
            Instant startedFrom,
            Instant startedTo
    ) {

        public Filter {
            name = name == null ? null : name.trim();
            countryId = countryId == null ? null : countryId.trim();
            categoryId = categoryId == null ? null : categoryId.trim();
            tagIds = tagIds == null ? null : Set.copyOf(tagIds);
        }
    }

    public record OrderBy(
            boolean startTimeDirectionAsc
    ) {
    }
}
