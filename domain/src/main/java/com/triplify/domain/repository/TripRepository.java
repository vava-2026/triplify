package com.triplify.domain.repository;

import com.triplify.domain.filter.TripFilter;
import com.triplify.domain.model.Trip;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.Optional;
import java.util.Set;

public interface TripRepository {
    Optional<Trip> findById(String id);
    Page<Trip> findList(PageRequest pageRequest, TripFilter filter, boolean startTimeAsc);
    void create(Trip trip);
    void update(Trip trip);
    void delete(Trip trip);
    void replaceTagIds(String tripId, Set<String> tagIds);
    void replaceCountryIds(String tripId, Set<String> countryIds);
    void updateCoverImageId(String tripId, String coverImageId);
}
