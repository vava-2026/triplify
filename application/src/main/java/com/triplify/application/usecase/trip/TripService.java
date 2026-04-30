package com.triplify.application.usecase.trip;

import com.triplify.application.usecase.trip.dto.AddTripRequest;
import com.triplify.application.usecase.trip.dto.DeleteTripRequest;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.GetTripsRequest;
import com.triplify.application.usecase.trip.dto.GetTripsForCalendarRequest;
import com.triplify.application.usecase.trip.dto.GetUndatedTripsRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.trip.dto.UpdateTripRequest;
import com.triplify.application.usecase.trip.dto.UpdateTripStatusRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

import java.util.List;

public interface TripService {

    Result<TripResponse> addTrip(AddTripRequest request);

    Result<TripResponse> updateTrip(UpdateTripRequest request);

    Result<Void> deleteTrip(DeleteTripRequest request);

    Result<TripResponse> updateStatus(UpdateTripStatusRequest request);

    Result<TripResponse> getTripById(GetTripByIdRequest request);

    Result<Page<TripResponse>> getTrips(GetTripsRequest request);

    Result<List<TripResponse>> getTripsForCalendar(GetTripsForCalendarRequest request);

    Result<Page<TripResponse>> getUndatedTrips(GetUndatedTripsRequest request);
}
