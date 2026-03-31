package com.triplify.application.usecase.map.dto;

public record MapObjectResponse(
        String id,
        GetMapObjectsRequest.Filter.MapObjectType objectType,
        String title,
        double latitude,
        double longitude
) {
}

