package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Emotion;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.EmotionRepository;
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

public class EmotionRepositoryImpl implements EmotionRepository {

    private static final Logger log = LoggerFactory.getLogger(EmotionRepositoryImpl.class);

    @Override
    public Optional<Emotion> findById(UUID id) {
        String sql = """
            SELECT id, created_by, name, name_sk, emoji_unicode
            FROM emotions
            WHERE id = ?
            LIMIT 1
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find emotion by id='{}'", id, e);
            throw new RuntimeException("Database error while finding emotion by id", e);
        }

        return Optional.empty();
    }

    @Override
    public void create(Emotion emotion) {
        String sql = """
            INSERT INTO emotions (id, created_by, name, name_sk, emoji_unicode)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emotion.getId().toString());
            ps.setString(2, emotion.getCreatedById().toString());
            ps.setString(3, emotion.getName());
            ps.setString(4, emotion.getNameSk());
            ps.setString(5, emotion.getEmojiUnicode());
            ps.executeUpdate();

            log.debug("Created emotion id='{}', name='{}'", emotion.getId(), emotion.getName());
        } catch (SQLException e) {
            log.error("Failed to create emotion id='{}'", emotion.getId(), e);
            throw new RuntimeException("Database error while creating emotion", e);
        }
    }

    @Override
    public void update(Emotion emotion) {
        String sql = """
            UPDATE emotions
            SET name = ?, name_sk = ?, emoji_unicode = ?
            WHERE id = ?
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emotion.getName());
            ps.setString(2, emotion.getNameSk());
            ps.setString(3, emotion.getEmojiUnicode());
            ps.setString(4, emotion.getId().toString());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                log.warn("Update affected 0 rows for emotion id='{}'", emotion.getId());
            } else {
                log.debug("Updated emotion id='{}', name='{}'", emotion.getId(), emotion.getName());
            }
        } catch (SQLException e) {
            log.error("Failed to update emotion id='{}'", emotion.getId(), e);
            throw new RuntimeException("Database error while updating emotion", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM emotions WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                log.warn("Delete affected 0 rows for emotion id='{}'", id);
            } else {
                log.debug("Deleted emotion id='{}'", id);
            }
        } catch (SQLException e) {
            log.error("Failed to delete emotion id='{}'", id, e);
            throw new RuntimeException("Database error while deleting emotion", e);
        }
    }

    @Override
    public Optional<Emotion> findByName(String name) {
        String sql = """
            SELECT id, created_by, name, name_sk, emoji_unicode
            FROM emotions
            WHERE name = ?
            LIMIT 1
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find emotion by name='{}'", name, e);
            throw new RuntimeException("Database error while finding emotion by name", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Page<Emotion>> findAll(PageRequest page) {
        String sql = """
            SELECT id, created_by, name, name_sk, emoji_unicode
            FROM emotions
            ORDER BY name COLLATE NOCASE, id
            LIMIT ? OFFSET ?
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, page.size() + 1);
            ps.setInt(2, page.offset());
            try (ResultSet rs = ps.executeQuery()) {
                List<Emotion> emotions = new ArrayList<>();
                while (rs.next()) {
                    emotions.add(mapRow(rs));
                }

                boolean hasNext = emotions.size() > page.size();
                if (hasNext) {
                    emotions.removeLast();
                }

                return Optional.of(Page.of(emotions, page, hasNext));
            }
        } catch (SQLException e) {
            log.error("Failed to find all emotions page={}", page.page(), e);
            throw new RuntimeException("Database error while finding all emotions", e);
        }
    }

    private Emotion mapRow(ResultSet rs) throws SQLException {
        return new Emotion(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("created_by")),
                rs.getString("name"),
                rs.getString("name_sk"),
                rs.getString("emoji_unicode")
        );
    }
}
