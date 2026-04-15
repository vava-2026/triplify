package com.triplify.application.usecase.place.details;

import com.triplify.application.usecase.place.details.dto.GetPlaceDetailsRequest;
import com.triplify.application.usecase.place.details.dto.PlaceDetailsResponse;
import com.triplify.domain.result.Result;

public interface PlaceDetailsService {

    Result<PlaceDetailsResponse> getPlaceDetails(GetPlaceDetailsRequest request);
}
