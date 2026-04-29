package com.triplify.domain.repository;

import com.triplify.domain.filter.TripFilter;
import com.triplify.domain.model.Trip;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TripRepository {
    Optional<Trip> findById(UUID id);
    Page<Trip> findList(PageRequest pageRequest, TripFilter filter, boolean startTimeAsc, UUID userId);
    void create(Trip trip);
    void update(Trip trip);
    void delete(Trip trip);
    void replaceTagIds(UUID tripId, Set<UUID> tagIds);
    void replaceCountryIds(UUID tripId, Set<UUID> countryIds);
    void updateCoverImageId(UUID tripId, UUID coverImageId);
}
