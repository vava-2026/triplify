package com.triplify.domain.repository;

import com.triplify.domain.model.TripRoute;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.Optional;

public interface TripRouteRepository {
    Optional<TripRoute> findById(String id);
    Optional<TripRoute> findByTripIdAndRouteId(String tripId, String routeId);
    Page<TripRoute> findList(PageRequest pageRequest, String tripId, StatusEnum status);
    void create(TripRoute tripRoute);
    void update(TripRoute tripRoute);
    void delete(TripRoute tripRoute);
}
