package com.triplify.application.usecase.map;

import com.triplify.application.usecase.map.dto.GetMapObjectsRequest;
import com.triplify.application.usecase.map.dto.MapObjectResponse;
import com.triplify.domain.result.Result;

import java.util.List;

public interface MapService {

    Result<List<MapObjectResponse>> getMapObjects(GetMapObjectsRequest request);
}
