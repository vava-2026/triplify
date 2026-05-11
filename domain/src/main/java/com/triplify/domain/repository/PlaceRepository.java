package com.triplify.domain.repository;

import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.model.Place;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PlaceRepository {
    Optional<Place> findById(UUID id);
    List<Place> findByIds(Set<UUID> ids);
    Page<Place> findList(PageRequest page, PlaceFilter filter, UUID userId);
    void create(Place place);
    void update(Place place);
    void delete(Place place);
}
