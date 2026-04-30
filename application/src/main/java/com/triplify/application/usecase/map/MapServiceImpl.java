package com.triplify.application.usecase.map;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.map.dto.GetClusterItemsRequest;
import com.triplify.application.usecase.map.dto.GetMapObjectsRequest;
import com.triplify.application.usecase.map.dto.MapObjectResponse;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.map.MapDataPoint;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.MapRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Authenticated
public class MapServiceImpl implements MapService {

    private static final Logger log = LoggerFactory.getLogger(MapServiceImpl.class);
    private static final double GRID_DIVISOR = 4.0;
    private static final double MIN_GRID_SIZE = 0.001;
    private static final double MAX_GRID_SIZE = 45.0;

    private final MapRepository mapRepository;
    private final UserSessionContext userSessionContext;

    @Inject
    public MapServiceImpl(MapRepository mapRepository, UserSessionContext userSessionContext) {
        this.mapRepository = mapRepository;
        this.userSessionContext = userSessionContext;
    }

    @Override
    public Result<List<MapObjectResponse>> getMapObjects(GetMapObjectsRequest request) {
        UUID userId = userSessionContext.getCurrent().orElseThrow().userId();
        log.info("Getting map objects for userId='{}', minLatitude='{}', maxLatitude='{}', minLongitude='{}', maxLongitude='{}', zoomLevel='{}'", userId, request.minLatitude(), request.maxLatitude(), request.minLongitude(), request.maxLongitude(), request.zoomLevel());
        double gridSize = computeGridSize(request.zoomLevel());

        List<MapDataPoint> points = mapRepository.getMapMarkers(
                userId,
                request.minLatitude(), request.minLongitude(),
                request.maxLatitude(), request.maxLongitude(),
                gridSize,
                request.filter()
        );
        log.info("Found {} map objects", points.size());
        return Result.ok(points.stream().map(MapObjectResponse::from).toList());
    }

    @Override
    public Result<Page<MapObjectResponse>> getClusterItems(GetClusterItemsRequest request) {
        log.info("Getting cluster items for userId='{}', clusterLatitude='{}', clusterLongitude='{}', zoomLevel='{}'", userSessionContext.getCurrent().orElseThrow().userId(), request.clusterLatitude(), request.clusterLongitude(), request.zoomLevel());
        UUID userId = userSessionContext.getCurrent().orElseThrow().userId();
        double gridSize = computeGridSize(request.zoomLevel());

        Page<MapDataPoint> page = mapRepository.getClusterItems(
                userId,
                request.clusterLatitude(), request.clusterLongitude(),
                gridSize,
                request.filter(),
                request.pageRequest()
        );
        log.info("Found {} items in cluster", page.items().size());
        return Result.ok(page.map(MapObjectResponse::from));
    }


    private double computeGridSize(double zoomLevel) {
        double raw = 360.0 / Math.pow(2, zoomLevel) / GRID_DIVISOR;
        return Math.max(MIN_GRID_SIZE, Math.min(MAX_GRID_SIZE, raw));
    }
}
