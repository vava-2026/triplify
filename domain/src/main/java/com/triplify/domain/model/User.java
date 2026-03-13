package com.triplify.domain.model;

import com.triplify.domain.model.enums.RoleEnum;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public class User {
    private final UUID id;
    private String username;
    private String email;
    @ToString.Exclude private String passwordHash;
    private RoleEnum role;
    private Image avatarImage;
    private final Instant createdAt;
    private Instant updatedAt;

    public User(@NonNull String username, @NonNull String email, @NonNull String passwordHash) {
        if (username.isBlank()) throw new IllegalArgumentException("Username must not be blank.");
        if (email.isBlank()) throw new IllegalArgumentException("Email must not be blank.");
        if (passwordHash.isBlank()) throw new IllegalArgumentException("Password hash must not be blank.");
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = RoleEnum.USER;
        this.updatedAt = Instant.now();
    }

    public void updateUsername(String username) {
        this.username = username;
        this.updatedAt = Instant.now();
    }

    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void updatePasswordHash(@NonNull String newPasswordHash) throws IllegalArgumentException {
        if (newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank.");
        }
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public void updateAvatar(Image image) {
        this.avatarImage = image;
        this.updatedAt = Instant.now();
    }

    public void removeAvatar() {
        this.avatarImage = null;
        this.updatedAt = Instant.now();
    }

    public void promoteRole(RoleEnum newRole) throws IllegalArgumentException {
        if (newRole == null) throw new IllegalArgumentException("Role must not be null.");
        this.role = newRole;
        this.updatedAt = Instant.now();
    }
}
