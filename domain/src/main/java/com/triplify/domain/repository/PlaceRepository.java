package com.triplify.domain.repository;

import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.model.Place;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.Optional;

public interface PlaceRepository {
    Optional<Place> findById(String id);
    Page<Place> findList(PageRequest page, PlaceFilter filter);
    void create(Place place);
    void update(Place place);
    void delete(Place place);
}
