package com.triplify.infrastructure.repository;

import com.triplify.domain.model.BadgeGroup;
import com.triplify.domain.repository.BadgeGroupRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BadgeGroupRepositoryImpl implements BadgeGroupRepository {

    private static final Logger log = LoggerFactory.getLogger(BadgeGroupRepositoryImpl.class);

    @Override
    public List<BadgeGroup> findAll() {
        String sql = "SELECT id, name, name_sk, description, description_sk, created_by FROM badges_groups";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<BadgeGroup> groups = new ArrayList<>();
            while (rs.next()) {
                groups.add(mapRow(rs));
            }
            return groups;
        } catch (SQLException e) {
            log.error("Failed to find all badge groups", e);
            throw new RuntimeException("Database error while finding all badge groups", e);
        }
    }

    @Override
    public Optional<BadgeGroup> findById(UUID id) {
        String sql = "SELECT id, name, name_sk, description, description_sk, created_by FROM badges_groups WHERE id = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find badge group by id='{}'", id, e);
            throw new RuntimeException("Database error while finding badge group by id", e);
        }

        return Optional.empty();
    }

    private BadgeGroup mapRow(ResultSet rs) throws SQLException {
        String createdByRaw = rs.getString("created_by");
        UUID createdById = (createdByRaw == null || createdByRaw.isBlank())
                ? null
                : UUID.fromString(createdByRaw);

        return new BadgeGroup(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("name_sk"),
                rs.getString("description"),
                rs.getString("description_sk"),
                createdById
        );
    }
}
