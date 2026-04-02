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
@AllArgsConstructor
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

    @Setter(AccessLevel.PRIVATE)
    private Image avatarImage;

    @NonNull
    private final Instant createdAt;

    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    public User(@NonNull String username,
                @NonNull String email,
                @NonNull String passwordHash,
                @NonNull RoleEnum role) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
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
        setAvatarImage(null);
        setUpdatedAt(Instant.now());
    }

    public void updateAvatar(@NonNull Image avatarImage) {
        log.debug("User [{}] avatar linked: {}", id, avatarImage.getId());
        setAvatarImageId(avatarImage.getId());
        setAvatarImage(avatarImage);
        setUpdatedAt(Instant.now());
    }

    public void removeAvatar() {
        log.debug("User [{}] avatar removed.", id);
        setAvatarImageId(null);
        setAvatarImage(null);
        setUpdatedAt(Instant.now());
    }

    public void promoteRole(@NonNull RoleEnum newRole) {
        log.debug("User [{}] role: {} to {}", id, this.role, newRole);
        setRole(newRole);
        setUpdatedAt(Instant.now());
    }
}
