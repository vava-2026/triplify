package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Tag;
import com.triplify.domain.model.enums.ColorEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
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
    public Optional<Tag> findByUserIdAndName(String userId, String name) {
        String sql = """
            SELECT id, user_id, name, color
            FROM tags
            WHERE user_id = ? AND name = ?
            LIMIT 1
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find tag by userId='{}' and name='{}'", userId, name, e);
            throw new RuntimeException("Database error while finding tag by name", e);
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

    @Override
    public Page<Tag> findList(PageRequest pageRequest, String name) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, user_id, name, color
            FROM tags
            WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sql.append(" AND name LIKE ? ");
            params.add(name + "%");
        }

        sql.append(" ORDER BY name COLLATE NOCASE ASC, id ASC LIMIT ? OFFSET ?");
        params.add(pageRequest.size() + 1);
        params.add(pageRequest.offset());

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Tag> tags = new ArrayList<>();
                while (rs.next()) {
                    tags.add(mapRow(rs));
                }

                boolean hasNext = tags.size() > pageRequest.size();
                if (hasNext) {
                    tags.removeLast();
                }

                return Page.of(tags, pageRequest, hasNext);
            }
        } catch (SQLException e) {
            log.error("Failed to find tags by name='{}'", name, e);
            throw new RuntimeException("Database error while finding tags", e);
        }
    }

    @Override
    public void create(Tag tag) {
        String sql = """
            INSERT INTO tags (id, user_id, name, color)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tag.getId().toString());
            ps.setString(2, tag.getUserId().toString());
            ps.setString(3, tag.getName());
            ps.setString(4, tag.getColor().getValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to create tag id='{}'", tag.getId(), e);
            throw new RuntimeException("Database error while creating tag", e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM tags WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                log.warn("Delete affected 0 rows for tag id='{}'", id);
            } else {
                log.debug("Tag deleted: id={}", id);
            }
        } catch (SQLException e) {
            log.error("Failed to delete tag id='{}'", id, e);
            throw new RuntimeException("Database error while deleting tag", e);
        }
    }

    @Override
    public void update(Tag tag) {
        String sql = """
            UPDATE tags
            SET name = ?, color = ?
            WHERE id = ?
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tag.getName());
            ps.setString(2, tag.getColor().getValue());
            ps.setString(3, tag.getId().toString());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                log.warn("Update affected 0 rows for tag id='{}'", tag.getId());
            } else {
                log.debug("Tag updated: id={}", tag.getId());
            }
        } catch (SQLException e) {
            log.error("Failed to update tag id='{}'", tag.getId(), e);
            throw new RuntimeException("Database error while updating tag", e);
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
