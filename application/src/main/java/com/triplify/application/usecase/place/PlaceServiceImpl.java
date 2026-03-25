package com.triplify.application.usecase.place;

import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaceServiceImpl implements PlaceService {

    private static final Logger log = LoggerFactory.getLogger(PlaceServiceImpl.class);

    @Override
    public Result<Void> addPlace(AddPlaceRequest request) {
        log.info(
                "Mock add place request accepted: tripId={}, title='{}', country='{}', latitude={}, longitude={}, coverImagePath={}",
                request.tripId(),
                request.title(),
                request.country(),
                request.latitude(),
                request.longitude(),
                request.coverImagePath()
        );

        return Result.ok();
    }
}
