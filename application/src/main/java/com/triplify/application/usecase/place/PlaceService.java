package com.triplify.application.usecase.place;

import com.triplify.domain.result.Result;

public interface PlaceService {

    Result<Void> addPlace(AddPlaceRequest request);
}
