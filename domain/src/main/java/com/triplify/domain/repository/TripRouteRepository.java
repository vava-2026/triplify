package com.triplify.domain.repository;

import com.triplify.domain.model.TripRoute;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.Optional;
import java.util.UUID;

public interface TripRouteRepository {
    Optional<TripRoute> findById(UUID id);
    Optional<TripRoute> findByTripIdAndRouteId(UUID tripId, UUID routeId);
    Page<TripRoute> findList(PageRequest pageRequest, UUID tripId, StatusEnum status);
    void create(TripRoute tripRoute);
    void update(TripRoute tripRoute);
    void delete(TripRoute tripRoute);
}
