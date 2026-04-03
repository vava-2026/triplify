package com.triplify.application.service;

import com.triplify.application.request.SearchPlacesRequest;
import com.triplify.application.response.SearchPlacesResponse;

public interface PlaceService {
    SearchPlacesResponse searchPlaces(SearchPlacesRequest request);
}
