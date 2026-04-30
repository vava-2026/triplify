package com.triplify.domain.repository;

import com.triplify.domain.map.MapDataPoint;
import com.triplify.domain.map.MapObjectType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MapRepository {

    List<MapDataPoint> getMapMarkers(
            UUID userId,
            double minLatitude, double minLongitude,
            double maxLatitude, double maxLongitude,
            double gridSize,
            Set<MapObjectType> filter
    );

    Page<MapDataPoint> getClusterItems(
            UUID userId,
            double clusterLatitude, double clusterLongitude,
            double gridSize,
            Set<MapObjectType> filter,
            PageRequest pageRequest
    );
}
