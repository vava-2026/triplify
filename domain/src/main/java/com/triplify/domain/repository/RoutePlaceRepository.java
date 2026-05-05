package com.triplify.domain.repository;

import com.triplify.domain.model.RoutePlace;
import com.triplify.domain.model.RouteWithPlaces;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutePlaceRepository {
    Optional<RoutePlace> findByRouteIdAndPlaceId(UUID routeId, UUID placeId);
    List<RoutePlace> findByRouteId(UUID routeId);
    List<RoutePlace> findByPlaceId(UUID placeId);
    Page<RouteWithPlaces> findRoutesWithPlacesByPlaceId(PageRequest pageRequest, UUID placeId, UUID userId);
    void create(RoutePlace routePlace);
    void update(RoutePlace routePlace);
    void delete(RoutePlace routePlace);
}
