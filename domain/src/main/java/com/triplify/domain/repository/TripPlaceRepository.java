package com.triplify.domain.repository;

import com.triplify.domain.model.TripPlace;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.time.Instant;
import java.util.Optional;

public interface TripPlaceRepository {
    Optional<TripPlace> findById(String id);
    Optional<TripPlace> findByTripIdAndPlaceId(String tripId, String placeId);
    Page<TripPlace> findList(
            PageRequest pageRequest,
            String tripId,
            TripPlaceSourceType sourceType,
            String tripRouteId,
            String routePlaceId,
            Instant visitFrom,
            Instant visitTo,
            boolean visitTimeAsc
    );
    void create(TripPlace tripPlace);
    void update(TripPlace tripPlace);
    void delete(TripPlace tripPlace);
}
