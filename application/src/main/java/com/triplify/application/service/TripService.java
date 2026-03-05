package com.triplify.application.service;

import com.triplify.application.request.SearchTripsRequest;
import com.triplify.application.response.SearchTripsResponse;

public interface TripService {
    SearchTripsResponse searchTrips(SearchTripsRequest request);
}
