package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Badge;
import com.triplify.domain.repository.BadgeRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BadgeRepositoryImpl implements BadgeRepository {

    private static final Logger log = LoggerFactory.getLogger(BadgeRepositoryImpl.class);

    @Override
    public List<Badge> findAll(String groupId, String createdById) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, created_by, group_id, image_id, name, name_sk, description, description_sk, level, required_value " +
                        "FROM badges WHERE 1=1"
        );
        List<String> params = new ArrayList<>();

        if (groupId != null && !groupId.isBlank()) {
            sql.append(" AND group_id = ?");
            params.add(groupId);
        }

        if (createdById != null && !createdById.isBlank()) {
            sql.append(" AND created_by = ?");
            params.add(createdById);
        }

        sql.append(" ORDER BY group_id, level, name");

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Badge> badges = new ArrayList<>();
                while (rs.next()) {
                    badges.add(mapRow(rs));
                }
                return badges;
            }
        } catch (SQLException e) {
            log.error("Failed to find badges with groupId='{}' and createdById='{}'", groupId, createdById, e);
            throw new RuntimeException("Database error while finding badges", e);
        }
    }

    @Override
    public Optional<Badge> findById(String id) {
        String sql = "SELECT id, created_by, group_id, image_id, name, name_sk, description, description_sk, level, required_value " +
                "FROM badges WHERE id = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find badge by id='{}'", id, e);
            throw new RuntimeException("Database error while finding badge by id", e);
        }

        return Optional.empty();
    }

    @Override
    public boolean existsByNameAndLevel(String groupId, String name, int level) {
        String sql = "SELECT COUNT(*) FROM badges WHERE group_id = ? AND name = ? AND level = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupId);
            ps.setString(2, name);
            ps.setInt(3, level);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Failed to check existence for badge groupId='{}', name='{}', level='{}'", groupId, name, level, e);
            throw new RuntimeException("Database error while checking badge existence", e);
        }
    }

    @Override
    public boolean existsByNameAndLevelExcludingId(String groupId, String name, int level, String excludedId) {
        String sql = "SELECT COUNT(*) FROM badges WHERE group_id = ? AND name = ? AND level = ? AND id <> ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupId);
            ps.setString(2, name);
            ps.setInt(3, level);
            ps.setString(4, excludedId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Failed to check existence for badge groupId='{}', name='{}', level='{}', excludedId='{}'", groupId, name, level, excludedId, e);
            throw new RuntimeException("Database error while checking badge existence", e);
        }
    }

    @Override
    public void create(Badge badge) {
        String sql = "INSERT INTO badges (id, created_by, group_id, image_id, name, name_sk, description, description_sk, level, required_value) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, badge.getId().toString());
            ps.setObject(2, badge.getCreatedById() != null ? badge.getCreatedById().toString() : null, Types.VARCHAR);
            ps.setString(3, badge.getGroupId().toString());
            ps.setObject(4, badge.getImageId() != null ? badge.getImageId().toString() : null, Types.VARCHAR);
            ps.setString(5, badge.getName());
            ps.setString(6, badge.getNameSk());
            ps.setString(7, badge.getDescription());
            ps.setString(8, badge.getDescriptionSk());
            ps.setInt(9, badge.getLevel());
            ps.setInt(10, badge.getRequiredValue());
            ps.executeUpdate();

            log.debug("Created badge with id='{}', name='{}'", badge.getId(), badge.getName());
        } catch (SQLException e) {
            log.error("Failed to create badge with id='{}'", badge.getId(), e);
            throw new RuntimeException("Database error while creating badge", e);
        }
    }

    @Override
    public void update(Badge badge) {
        String sql = "UPDATE badges SET group_id = ?, image_id = ?, name = ?, name_sk = ?, description = ?, description_sk = ?, level = ?, required_value = ? WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, badge.getGroupId().toString());
            ps.setObject(2, badge.getImageId() != null ? badge.getImageId().toString() : null, Types.VARCHAR);
            ps.setString(3, badge.getName());
            ps.setString(4, badge.getNameSk());
            ps.setString(5, badge.getDescription());
            ps.setString(6, badge.getDescriptionSk());
            ps.setInt(7, badge.getLevel());
            ps.setInt(8, badge.getRequiredValue());
            ps.setString(9, badge.getId().toString());
            ps.executeUpdate();

            log.debug("Updated badge with id='{}', name='{}'", badge.getId(), badge.getName());
        } catch (SQLException e) {
            log.error("Failed to update badge with id='{}'", badge.getId(), e);
            throw new RuntimeException("Database error while updating badge", e);
        }
    }

    @Override
    public void delete(Badge badge) {
        String sql = "DELETE FROM badges WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, badge.getId().toString());
            ps.executeUpdate();

            log.debug("Deleted badge with id='{}', name='{}'", badge.getId(), badge.getName());
        } catch (SQLException e) {
            log.error("Failed to delete badge with id='{}'", badge.getId(), e);
            throw new RuntimeException("Database error while deleting badge", e);
        }
    }

    private Badge mapRow(ResultSet rs) throws SQLException {
        String createdByRaw = rs.getString("created_by");
        UUID createdById = (createdByRaw == null || createdByRaw.isBlank()) ? null : UUID.fromString(createdByRaw);
        String imageIdRaw = rs.getString("image_id");
        UUID imageId = imageIdRaw == null ? null : UUID.fromString(imageIdRaw);

        return new Badge(
                UUID.fromString(rs.getString("id")),
                createdById,
                UUID.fromString(rs.getString("group_id")),
                imageId,
                null,
                rs.getString("name"),
                rs.getString("name_sk"),
                rs.getString("description"),
                rs.getString("description_sk"),
                rs.getInt("level"),
                rs.getInt("required_value")
        );
    }
}

