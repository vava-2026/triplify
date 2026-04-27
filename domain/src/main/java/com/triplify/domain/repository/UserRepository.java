package com.triplify.domain.repository;

import com.triplify.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void save(User user);
    void update(User user);
}
