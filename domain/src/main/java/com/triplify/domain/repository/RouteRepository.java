package com.triplify.domain.repository;

import com.triplify.domain.filter.RouteFilter;
import com.triplify.domain.model.Route;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RouteRepository {
    Optional<Route> findById(UUID id);
    List<Route> findByIds(Set<UUID> ids);
    Page<Route> findList(PageRequest page, String name, boolean lengthAsc, UUID userId);
    void create(Route route);
    void update(Route route);
    void delete(Route route);
}
