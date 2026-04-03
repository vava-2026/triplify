package com.triplify.application.request;

import com.triplify.application.pagination.Pagination;
import com.triplify.application.response.PlaceStatus;

public record SearchPlacesRequest(
        Integer    tripId,
        String name,
        PlaceStatus status,
        PlaceSort  sort,
        Pagination pagination
) {}
