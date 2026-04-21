package com.triplify.domain.repository;

import com.triplify.domain.model.RoutePlace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutePlaceRepository {
    Optional<RoutePlace> findByRouteIdAndPlaceId(UUID routeId, UUID placeId);
    List<RoutePlace> findByRouteId(UUID routeId);
    List<RoutePlace> findByPlaceId(UUID placeId);
    void create(RoutePlace routePlace);
    void update(RoutePlace routePlace);
    void delete(RoutePlace routePlace);
}
