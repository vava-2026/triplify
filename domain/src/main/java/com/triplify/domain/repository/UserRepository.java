package com.triplify.domain.repository;

import com.triplify.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean verifyPassword(String userId, String rawPassword);
}
