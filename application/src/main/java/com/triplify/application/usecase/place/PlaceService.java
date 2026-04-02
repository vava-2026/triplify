package com.triplify.application.usecase.place;

import com.triplify.application.usecase.place.dto.AddPlaceRequest;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.GetPlaceByIdRequest;
import com.triplify.application.usecase.place.dto.GetPlacesRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.place.dto.UpdatePlaceRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public interface PlaceService {

    Result<PlaceResponse> addPlace(AddPlaceRequest request);

    Result<PlaceResponse> updatePlace(UpdatePlaceRequest request);

    Result<Void> deletePlace(DeletePlaceRequest request);

    Result<PlaceResponse> getPlaceById(GetPlaceByIdRequest request);

    Result<Page<PlaceResponse>> getPlaces(GetPlacesRequest request);
}
