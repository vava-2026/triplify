package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Tag;
import com.triplify.domain.model.enums.ColorEnum;
import com.triplify.domain.repository.TagRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class TagRepositoryImpl implements TagRepository {

    private static final Logger log = LoggerFactory.getLogger(TagRepositoryImpl.class);

    @Override
    public Optional<Tag> findById(String id) {
        String sql = """
            SELECT id, user_id, name, color
            FROM tags
            WHERE id = ?
            LIMIT 1
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find tag by id='{}'", id, e);
            throw new RuntimeException("Database error while finding tag by id", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Tag> findByIds(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(", ", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = """
            SELECT id, user_id, name, color
            FROM tags
            WHERE id IN (%s)
            """.formatted(placeholders);

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            for (String id : ids) {
                ps.setString(index++, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Tag> tags = new ArrayList<>();
                while (rs.next()) {
                    tags.add(mapRow(rs));
                }
                return tags;
            }
        } catch (SQLException e) {
            log.error("Failed to find tags by ids", e);
            throw new RuntimeException("Database error while finding tags by ids", e);
        }
    }

    private Tag mapRow(ResultSet rs) throws SQLException {
        return new Tag(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                rs.getString("name"),
                parseColor(rs.getString("color"))
        );
    }

    private ColorEnum parseColor(String color) {
        if (color == null || color.isBlank()) {
            return ColorEnum.GRAY;
        }
        return ColorEnum.valueOf(color.trim().toUpperCase(Locale.ROOT));
    }
}
