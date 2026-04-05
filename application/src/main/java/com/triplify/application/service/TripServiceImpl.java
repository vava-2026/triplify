package com.triplify.application.service;

import com.triplify.application.pagination.Pagination;
import com.triplify.application.request.SearchTripsRequest;
import com.triplify.application.response.SearchTripsResponse;

import java.util.List;

/**
 * Legacy placeholder kept only so older UI code still compiles.
 * New trip flows use com.triplify.application.usecase.trip.TripService instead.
 */
public class TripServiceImpl implements TripService {

    @Override
    public SearchTripsResponse searchTrips(SearchTripsRequest request) {
        Pagination pagination = request == null || request.pagination() == null
                ? Pagination.request(1, 8).withTotals(0)
                : request.pagination().withTotals(0);
        return new SearchTripsResponse(List.of(), pagination);
    }
}
