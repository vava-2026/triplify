package com.triplify.application.request;

import com.triplify.application.pagination.Pagination;
import com.triplify.application.usecase.trip.dto.TripStatus;

public record SearchTripsRequest(
        String country,
        String category,
        String tag,
        TripStatus status,
        String startTime,
        TripSort sort,
        Pagination pagination
) {
    public static SearchTripsRequest empty(Pagination pagination) {
        return new SearchTripsRequest(
                null,
                null,
                null,
                null,
                null,
                TripSort.NEWEST_FIRST,
                pagination
        );
    }
}
