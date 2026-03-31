package com.triplify.domain.repository;

import com.triplify.domain.filter.CountryFilter;
import com.triplify.domain.model.Country;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;

import java.util.Optional;
import java.util.UUID;

public interface CountryRepository {
    Optional<Country> findById(String id);
    Page<Country> findAll(PageRequest page, CountryFilter filter);
    boolean existsByName(String name, String nameSk);
    void delete(Country country);
    void create(Country country);
    void update(Country country);
}
