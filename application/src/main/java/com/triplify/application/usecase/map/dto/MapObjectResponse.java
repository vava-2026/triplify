package com.triplify.application.usecase.map.dto;

import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.domain.map.MapDataPoint;
import com.triplify.domain.map.MapObjectType;

import java.time.Instant;

public record MapObjectResponse(
        String id,
        MapObjectType objectType,
        String title,
        double latitude,
        double longitude,
        ImageResponse coverImage,
        int count
) {
    public static MapObjectResponse from(MapDataPoint point) {
        ImageResponse coverImage = null;
        if (point.coverImageId() != null && point.coverImageUrl() != null) {
            coverImage = new ImageResponse(point.coverImageId(), point.coverImageUrl(), null, Instant.EPOCH);
        }
        return new MapObjectResponse(
                point.id() != null ? point.id().toString() : null,
                point.objectType(),
                point.title(),
                point.latitude(),
                point.longitude(),
                coverImage,
                point.count()
        );
    }
}
