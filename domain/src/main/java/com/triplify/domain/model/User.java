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
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class User {

    @EqualsAndHashCode.Include
    @NonNull
    private final UUID id = UUID.randomUUID();

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
    private final Instant createdAt = Instant.now();

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt = Instant.now();

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
