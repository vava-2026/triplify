package com.triplify.domain.model;

import com.triplify.domain.model.enums.RoleEnum;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String username;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String email;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    @ToString.Exclude
    private String passwordHash;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private RoleEnum role;

    @Setter(AccessLevel.PRIVATE)
    private UUID avatarImageId;

    @NonNull
    private final Instant createdAt;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    public static User of(@NonNull String username, @NonNull String email, @NonNull String passwordHash) {
        return new User(username, email, passwordHash);
    }

    private User(@NonNull String username, @NonNull String email, @NonNull String passwordHash) {
        if (username.isBlank()) throw new IllegalArgumentException("Username must not be blank.");
        if (email.isBlank()) throw new IllegalArgumentException("Email must not be blank.");
        if (passwordHash.isBlank()) throw new IllegalArgumentException("Password hash must not be blank.");
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = RoleEnum.USER;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        log.debug("User created: id={}, username={}, role={}", id, username, role);
    }

    public void updateUsername(@NonNull String username) {
        if (username.isBlank()) throw new IllegalArgumentException("Username must not be blank.");
        log.debug("User [{}] username: {} to {}", id, this.username, username);
        setUsername(username);
        setUpdatedAt(Instant.now());
    }

    public void updateEmail(@NonNull String email) {
        if (email.isBlank()) throw new IllegalArgumentException("Email must not be blank.");
        log.debug("User [{}] email: {} to {}", id, this.email, email);
        setEmail(email);
        setUpdatedAt(Instant.now());
    }

    public void updatePasswordHash(@NonNull String newPasswordHash) {
        if (newPasswordHash.isBlank()) throw new IllegalArgumentException("Password hash must not be blank.");
        log.debug("User [{}] passwordHash updated.", id);
        setPasswordHash(newPasswordHash);
        setUpdatedAt(Instant.now());
    }

    public void updateAvatar(@NonNull UUID avatarImageId) {
        log.debug("User [{}] avatar updated: {}", id, avatarImageId);
        setAvatarImageId(avatarImageId);
        setUpdatedAt(Instant.now());
    }

    public void removeAvatar() {
        log.debug("User [{}] avatar removed.", id);
        setAvatarImageId(null);
        setUpdatedAt(Instant.now());
    }

    public void promoteRole(@NonNull RoleEnum newRole) {
        log.debug("User [{}] role: {} to {}", id, this.role, newRole);
        setRole(newRole);
        setUpdatedAt(Instant.now());
    }
}
