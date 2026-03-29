package com.triplify.domain.repository;

import com.triplify.domain.model.Country;

import java.util.Optional;
import java.util.UUID;

public interface CountryRepository {
    Optional<Country> findById(String id);
    boolean existsByName(String name, String nameSk);
    void delete(Country country);
    void create(Country country);
    void update(Country country);
}
