package com.triplify.domain.repository;

import com.triplify.domain.model.TripPlace;
import com.triplify.domain.model.Trip;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripPlaceRepository {
    Optional<TripPlace> findById(UUID id);
    Optional<TripPlace> findByTripIdAndPlaceId(UUID tripId, UUID placeId);
    List<TripPlace> findByPlaceId(UUID placeId);
    Page<Trip> findTripsByPlaceId(PageRequest pageRequest, UUID placeId, UUID userId);
    Page<TripPlace> findList(
            PageRequest pageRequest,
            UUID tripId,
            TripPlaceSourceType sourceType,
            UUID tripRouteId,
            UUID routePlaceId,
            Instant visitFrom,
            Instant visitTo,
            boolean visitTimeAsc);
    void create(TripPlace tripPlace);
    void update(TripPlace tripPlace);
    void delete(TripPlace tripPlace);
}
