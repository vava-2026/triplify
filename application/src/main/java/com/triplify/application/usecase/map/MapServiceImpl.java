package com.triplify.application.usecase.map;

import com.triplify.application.shared.error.ApplicationError;
import com.triplify.application.usecase.map.dto.GetMapObjectsRequest;
import com.triplify.application.usecase.map.dto.MapObjectResponse;
import com.triplify.domain.result.Result;

import java.util.List;

public class MapServiceImpl implements MapService {

    @Override
    public Result<List<MapObjectResponse>> getMapObjects(GetMapObjectsRequest request) {
        // TODO: implement map object retrieval by coordinates, radius, z-level and object type.
        return Result.fail(new ApplicationError.Unexpected("TODO: MapService.getMapObjects"));
    }
}

