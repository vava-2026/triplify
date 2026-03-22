package com.triplify.infrastructure.repository;

import com.triplify.domain.model.User;
import com.triplify.domain.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private static final List<User> USERS = List.of(
            User.of("admin", "admin@triplify.com", ""),
            User.of("user", "user@triplify.com", "")
    );

    @Override
    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return USERS.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username)
                          || u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public boolean verifyPassword(String userId, String rawPassword) {
        // TODO: replace with real verification
        return "password123".equals(rawPassword);
    }
}
