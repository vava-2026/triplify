package com.triplify.domain.map;

import java.nio.file.Path;
import java.util.UUID;

public record MapDataPoint(
        UUID id,
        MapObjectType objectType,
        String title,
        double latitude,
        double longitude,
        UUID coverImageId,
        Path coverImageUrl,
        int count
) {}
