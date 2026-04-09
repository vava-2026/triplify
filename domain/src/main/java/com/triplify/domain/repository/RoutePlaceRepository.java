package com.triplify.domain.repository;

import com.triplify.domain.model.RoutePlace;

import java.util.List;
import java.util.Optional;

public interface RoutePlaceRepository {
    Optional<RoutePlace> findByRouteIdAndPlaceId(String routeId, String placeId);
    List<RoutePlace> findByRouteId(String routeId);
    void create(RoutePlace routePlace);
    void update(RoutePlace routePlace);
    void delete(RoutePlace routePlace);
}
