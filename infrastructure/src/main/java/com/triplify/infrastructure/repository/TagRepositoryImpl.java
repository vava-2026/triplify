package com.triplify.infrastructure.repository;

import com.triplify.domain.filter.TagFilter;
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
import java.util.Optional;
import java.util.UUID;

public class TagRepositoryImpl implements TagRepository {

    private static final Logger log = LoggerFactory.getLogger(TagRepositoryImpl.class);

    @Override
    public Page<Tag> findList(PageRequest pageRequest, TagFilter filter) {
        boolean hasNameFilter = filter.name() != null && !filter.name().isBlank();

        String whereClause = "WHERE user_id = ? " + (hasNameFilter ? "AND name LIKE ? " : "");

        String countSql = "SELECT COUNT(*) FROM tags " + whereClause;
        String dataSql = "SELECT id, user_id, name, color FROM tags " + whereClause +
                "ORDER BY name ASC LIMIT ? OFFSET ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            long total = countRows(conn, countSql, filter, hasNameFilter);
            if (total == 0) {
                return Page.empty(pageRequest);
            }

            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                int idx = 1;
                ps.setString(idx++, filter.userId().toString());
                if (hasNameFilter) {
                    ps.setString(idx++, filter.name() + "%");
                }
                ps.setInt(idx++, pageRequest.size());
                ps.setInt(idx, pageRequest.offset());

                try (ResultSet rs = ps.executeQuery()) {
                    List<Tag> tags = new ArrayList<>();
                    while (rs.next()) {
                        tags.add(mapRow(rs));
                    }
                    return Page.of(tags, pageRequest, total);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find tags for userId='{}'", filter.userId(), e);
            throw new RuntimeException("Database error while finding tags", e);
        }
    }

    @Override
    public Optional<Tag> findById(String id) {
        String sql = "SELECT id, user_id, name, color FROM tags WHERE id = ? LIMIT 1";

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
    public boolean existsByUserIdAndName(String userId, String name) {
        String sql = "SELECT COUNT(*) FROM tags WHERE user_id = ? AND name = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Failed to check tag existence for userId='{}', name='{}'", userId, name, e);
            throw new RuntimeException("Database error while checking tag existence", e);
        }
    }

    @Override
    public void create(Tag tag) {
        String sql = "INSERT INTO tags (id, user_id, name, color) VALUES (?, ?, ?, ?)";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tag.getId().toString());
            ps.setString(2, tag.getUserId().toString());
            ps.setString(3, tag.getName());
            ps.setString(4, colorToSql(tag.getColor()));
            ps.executeUpdate();

            log.debug("Created tag with id='{}', name='{}' for userId='{}'",
                    tag.getId(), tag.getName(), tag.getUserId());
        } catch (SQLException e) {
            log.error("Failed to create tag with id='{}'", tag.getId(), e);
            throw new RuntimeException("Database error while creating tag", e);
        }
    }

    @Override
    public void update(Tag tag) {
        String sql = "UPDATE tags SET name = ? WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tag.getName());
            ps.setString(2, tag.getId().toString());
            ps.executeUpdate();

            log.debug("Updated tag with id='{}', name='{}'", tag.getId(), tag.getName());
        } catch (SQLException e) {
            log.error("Failed to update tag with id='{}'", tag.getId(), e);
            throw new RuntimeException("Database error while updating tag", e);
        }
    }

    @Override
    public void delete(Tag tag) {
        String sql = "DELETE FROM tags WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tag.getId().toString());
            ps.executeUpdate();

            log.debug("Deleted tag with id='{}', name='{}'", tag.getId(), tag.getName());
        } catch (SQLException e) {
            log.error("Failed to delete tag with id='{}'", tag.getId(), e);
            throw new RuntimeException("Database error while deleting tag", e);
        }
    }

    private long countRows(Connection conn, String countSql, TagFilter filter, boolean hasNameFilter)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(countSql)) {
            int idx = 1;
            ps.setString(idx++, filter.userId().toString());
            if (hasNameFilter) {
                ps.setString(idx, filter.name() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    private Tag mapRow(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID userId = UUID.fromString(rs.getString("user_id"));
        String name = rs.getString("name");
        ColorEnum color = sqlToColor(rs.getString("color"));
        return new Tag(id, userId, name, color);
    }

    private static String colorToSql(ColorEnum color) {
        if (color == null) return "blue";
        return switch (color) {
            case RED -> "red";
            case ORANGE -> "orange";
            case YELLOW -> "yellow";
            case GREEN -> "green";
            case TEAL -> "blue";
            case BLUE -> "blue";
            case PURPLE -> "purple";
            case PINK -> "pink";
        };
    }

    private static ColorEnum sqlToColor(String color) {
        if (color == null) return ColorEnum.BLUE;
        return switch (color) {
            case "red" -> ColorEnum.RED;
            case "orange" -> ColorEnum.ORANGE;
            case "yellow" -> ColorEnum.YELLOW;
            case "green" -> ColorEnum.GREEN;
            case "blue" -> ColorEnum.BLUE;
            case "purple" -> ColorEnum.PURPLE;
            case "pink" -> ColorEnum.PINK;
            default -> ColorEnum.BLUE;
        };
    }
}
