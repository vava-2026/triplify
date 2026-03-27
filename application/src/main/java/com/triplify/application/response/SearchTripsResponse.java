package com.triplify.application.response;

import com.triplify.application.pagination.Pagination;

import java.util.List;

public record SearchTripsResponse(
        List<TripResponse> trips,
        Pagination pagination
) {
}
