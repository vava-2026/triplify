package com.triplify.application.usecase.tripplace;

import com.triplify.application.usecase.tripplace.dto.AddTripPlaceRequest;
import com.triplify.application.usecase.tripplace.dto.DeleteTripPlaceRequest;
import com.triplify.application.usecase.tripplace.dto.GetTripPlaceByIdRequest;
import com.triplify.application.usecase.tripplace.dto.GetTripPlacesRequest;
import com.triplify.application.usecase.tripplace.dto.ReplaceManualTripPlacesRequest;
import com.triplify.application.usecase.tripplace.dto.TripPlaceResponse;
import com.triplify.application.usecase.tripplace.dto.UpdateTripPlaceRequest;
import com.triplify.application.usecase.tripplace.dto.UpdateTripPlaceStatusRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

import java.util.List;
import java.util.UUID;

public interface TripPlaceService {

    Result<TripPlaceResponse> addTripPlace(AddTripPlaceRequest request);

    Result<TripPlaceResponse> updateTripPlace(UpdateTripPlaceRequest request);

    Result<TripPlaceResponse> updateTripPlaceStatus(UpdateTripPlaceStatusRequest request);

    Result<Void> deleteTripPlace(DeleteTripPlaceRequest request);

    Result<TripPlaceResponse> getTripPlaceById(GetTripPlaceByIdRequest request);

    Result<Page<TripPlaceResponse>> getTripPlaces(GetTripPlacesRequest request);

    Result<Void> replaceManualTripPlaces(ReplaceManualTripPlacesRequest request);

    Result<List<TripPlaceResponse>> getAllManualTripPlaces(UUID tripId);
}
