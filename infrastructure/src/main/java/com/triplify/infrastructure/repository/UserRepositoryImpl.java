package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Image;
import com.triplify.domain.model.User;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.repository.UserRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class UserRepositoryImpl implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private static final String USER_WITH_AVATAR_SELECT = """
                SELECT u.id AS user_id, u.username, u.email, u.password_hash, u.role, u.avatar_image_id,
                        u.created_at AS user_created_at, u.updated_at AS user_updated_at,
                        i.id AS image_id, i.url AS image_url, i.description AS image_description, i.uploaded_at AS image_uploaded_at
                FROM users u
                LEFT JOIN images i ON i.id = u.avatar_image_id 
                """;

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = USER_WITH_AVATAR_SELECT + "WHERE u.email = ? LIMIT 1";
        
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find user by email='{}'", email, e); 
            throw new RuntimeException("Database error while finding user by email", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Failed to check username existence for username='{}'", username, e);
            throw new RuntimeException("Database error while checking username existence", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Failed to check email existence for email='{}'", email, e);
            throw new RuntimeException("Database error while checking email existence", e);
        }
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (id, username, email, password_hash, role, avatar_image_id, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, user.getId().toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole().getValue());
            ps.setString(6, user.getAvatarImageId() != null ? user.getAvatarImageId().toString() : null);
            ps.setString(7, user.getCreatedAt().toString());
            ps.setString(8, user.getUpdatedAt().toString());
            ps.executeUpdate();
            
            log.debug("User saved: id={}, username={}", user.getId(), user.getUsername());
        } catch (SQLException e) {
            log.error("Failed to save user username='{}'", user.getUsername(), e);
            throw new RuntimeException("Database error while saving user", e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password_hash = ?, role = ?, avatar_image_id = ?, updated_at = ? " +
                "WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().getValue());
            ps.setString(5, user.getAvatarImageId() != null ? user.getAvatarImageId().toString() : null);
            ps.setString(6, user.getUpdatedAt().toString());
            ps.setString(7, user.getId().toString());
            ps.executeUpdate();

            log.debug("User updated: id={}, username={}", user.getId(), user.getUsername());
        } catch (SQLException e) {
            log.error("Failed to update user id='{}'", user.getId(), e);
            throw new RuntimeException("Database error while updating user", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("user_id"));
        String username = rs.getString("username");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        RoleEnum role = RoleEnum.fromValue(rs.getString("role"));
        String avatarRaw = rs.getString("avatar_image_id");
        UUID avatarImageId = avatarRaw != null ? UUID.fromString(avatarRaw) : null;
        Image avatarImage = mapAvatarImage(rs);
        Instant createdAt = Instant.parse(rs.getString("user_created_at"));
        Instant updatedAt = Instant.parse(rs.getString("user_updated_at"));

        return new User(id, username, email, passwordHash, role, avatarImageId, avatarImage, createdAt, updatedAt);
    }

    private Image mapAvatarImage(ResultSet rs) throws SQLException {
        String imageIdRaw = rs.getString("image_id");
        if (imageIdRaw == null) {
            return null;
        }

        UUID imageId = UUID.fromString(imageIdRaw);
        Path imageUrl = Path.of(rs.getString("image_url"));
        String imageDescription = rs.getString("image_description");
        Instant imageUploadedAt = Instant.parse(rs.getString("image_uploaded_at"));
        return new Image(imageId, imageUrl, imageDescription, imageUploadedAt);
    }
}
