package com.triplify.infrastructure.repository;

import com.triplify.domain.filter.StoryFilter;
import com.triplify.domain.model.Emotion;
import com.triplify.domain.model.Story;
import com.triplify.domain.model.Tag;
import com.triplify.domain.model.enums.ColorEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.StoryRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import com.triplify.infrastructure.repository.utils.RepositoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StoryRepositoryImpl implements StoryRepository {

    private static final Logger log = LoggerFactory.getLogger(StoryRepositoryImpl.class);

    private static final String SELECT_STORY =
            "SELECT s.id, s.user_id, s.trip_id, s.trip_route_id, s.trip_place_id, " +
            "s.emotion_id, s.title, s.description, s.story_time, s.latitude, s.longitude, s.created_at, " +
            "e.id AS e_id, e.created_by AS e_created_by, e.name AS e_name, " +
            "e.name_sk AS e_name_sk, e.emoji_unicode AS e_emoji_unicode " +
            "FROM stories s " +
            "LEFT JOIN emotions e ON s.emotion_id = e.id ";

    @Override
    public Optional<Story> findById(UUID id) {
        String sql = SELECT_STORY + "WHERE s.id = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Story story = mapRow(rs);
                loadTags(conn, story);
                return Optional.of(story);
            }
        } catch (SQLException e) {
            log.error("Failed to find story by id='{}'", id, e);
            throw new RuntimeException("Database error while finding story by id", e);
        }
    }

    @Override
    public Page<Story> findList(PageRequest pageRequest, StoryFilter filter) {
        StringBuilder where = new StringBuilder("WHERE s.user_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(filter.userId().toString());

        if (filter.tripId() != null) {
            where.append("AND s.trip_id = ? ");
            params.add(filter.tripId());
        }
        if (filter.tripRouteId() != null) {
            where.append("AND s.trip_route_id = ? ");
            params.add(filter.tripRouteId());
        }
        if (filter.tripPlaceId() != null) {
            where.append("AND s.trip_place_id = ? ");
            params.add(filter.tripPlaceId());
        }
        if (filter.title() != null && !filter.title().isBlank()) {
            where.append("AND (s.title LIKE ? OR s.description LIKE ?) ");
            params.add("%" + filter.title() + "%");
            params.add("%" + filter.title() + "%");
        }
        if (filter.storyTimeFrom() != null) {
            where.append("AND s.story_time >= ? ");
            params.add(filter.storyTimeFrom().toString());
        }
        if (filter.storyTimeTo() != null) {
            where.append("AND s.story_time <= ? ");
            params.add(filter.storyTimeTo().toString());
        }

        String order = filter.storyTimeAsc() ? "ASC" : "DESC";
        String countSql = "SELECT COUNT(*) FROM stories s " + where;
        String dataSql  = SELECT_STORY + where + "ORDER BY s.story_time " + order + " LIMIT ? OFFSET ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            long total = countRows(conn, countSql, params);
            if (total == 0) return Page.empty(pageRequest);

            List<Story> stories = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                RepositoryUtils.bindParams(ps, params, 1);
                ps.setInt(params.size() + 1, pageRequest.size());
                ps.setInt(params.size() + 2, pageRequest.offset());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        stories.add(mapRow(rs));
                    }
                }
            }
            for (Story story : stories) {
                loadTags(conn, story);
            }

            boolean hasNext = total > pageRequest.offset() + stories.size();
            return Page.of(stories, pageRequest, hasNext);
        } catch (SQLException e) {
            log.error("Failed to find stories for userId='{}'", filter.userId(), e);
            throw new RuntimeException("Database error while finding stories", e);
        }
    }

    @Override
    public void create(Story story) {
        String sql = "INSERT INTO stories " +
                "(id, user_id, trip_id, trip_route_id, trip_place_id, emotion_id, title, description, story_time, latitude, longitude, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteConnectionFactory.getSpatialConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1,  story.getId().toString());
                ps.setString(2,  story.getUserId().toString());
                ps.setString(3,  RepositoryUtils.uuidOrNull(story.getTripId()));
                ps.setString(4,  RepositoryUtils.uuidOrNull(story.getTripRouteId()));
                ps.setString(5,  RepositoryUtils.uuidOrNull(story.getTripPlaceId()));
                ps.setString(6,  RepositoryUtils.uuidOrNull(story.getEmotionId()));
                ps.setString(7,  story.getTitle());
                ps.setString(8,  story.getDescription());
                ps.setString(9,  story.getStoryTime().toString());
                if (story.getLatitude() != null) ps.setDouble(10, story.getLatitude()); else ps.setNull(10, java.sql.Types.REAL);
                if (story.getLongitude() != null) ps.setDouble(11, story.getLongitude()); else ps.setNull(11, java.sql.Types.REAL);
                ps.setString(12, story.getCreatedAt().toString());
                ps.executeUpdate();
            }
            syncTags(conn, story);
            log.debug("Created story id='{}', title='{}'", story.getId(), story.getTitle());
        } catch (SQLException e) {
            log.error("Failed to create story id='{}'", story.getId(), e);
            throw new RuntimeException("Database error while creating story", e);
        }
    }

    @Override
    public void update(Story story) {
        String sql = "UPDATE stories SET title = ?, description = ?, story_time = ?, emotion_id = ?, latitude = ?, longitude = ? WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getSpatialConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, story.getTitle());
                ps.setString(2, story.getDescription());
                ps.setString(3, story.getStoryTime().toString());
                ps.setString(4, RepositoryUtils.uuidOrNull(story.getEmotionId()));
                if (story.getLatitude() != null) ps.setDouble(5, story.getLatitude()); else ps.setNull(5, java.sql.Types.REAL);
                if (story.getLongitude() != null) ps.setDouble(6, story.getLongitude()); else ps.setNull(6, java.sql.Types.REAL);
                ps.setString(7, story.getId().toString());
                ps.executeUpdate();
            }
            syncTags(conn, story);
            log.debug("Updated story id='{}', title='{}'", story.getId(), story.getTitle());
        } catch (SQLException e) {
            log.error("Failed to update story id='{}'", story.getId(), e);
            throw new RuntimeException("Database error while updating story", e);
        }
    }

    @Override
    public void delete(Story story) {
        String sql = "DELETE FROM stories WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, story.getId().toString());
            ps.executeUpdate();
            log.debug("Deleted story id='{}', title='{}'", story.getId(), story.getTitle());
        } catch (SQLException e) {
            log.error("Failed to delete story id='{}'", story.getId(), e);
            throw new RuntimeException("Database error while deleting story", e);
        }
    }

    private Story mapRow(ResultSet rs) throws SQLException {
        Emotion emotion = null;
        String emotionId = rs.getString("e_id");
        if (emotionId != null) {
            emotion = new Emotion(
                    UUID.fromString(rs.getString("e_id")),
                    UUID.fromString(rs.getString("e_created_by")),
                    rs.getString("e_name"),
                    rs.getString("e_name_sk"),
                    rs.getString("e_emoji_unicode")
            );
        }

        Double latitude  = rs.getObject("latitude")  != null ? rs.getDouble("latitude")  : null;
        Double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;

        return new Story(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                RepositoryUtils.parseUuid(rs.getString("trip_id")),
                RepositoryUtils.parseUuid(rs.getString("trip_route_id")),
                RepositoryUtils.parseUuid(rs.getString("trip_place_id")),
                RepositoryUtils.parseUuid(rs.getString("emotion_id")),
                emotion,
                rs.getString("title"),
                rs.getString("description"),
                Instant.parse(rs.getString("story_time")),
                latitude,
                longitude,
                Instant.parse(rs.getString("created_at")),
                new LinkedHashSet<>(),
                new LinkedHashSet<>()
        );
    }

    private void loadTags(Connection conn, Story story) throws SQLException {
        String sql = "SELECT t.id, t.user_id, t.name, t.color " +
                "FROM story_tags st JOIN tags t ON st.tag_id = t.id " +
                "WHERE st.story_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, story.getId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID tagId    = UUID.fromString(rs.getString("id"));
                    UUID tagUserId = UUID.fromString(rs.getString("user_id"));
                    String tagName = rs.getString("name");
                    ColorEnum color = RepositoryUtils.sqlToColor(rs.getString("color"));
                    story.addTag(new Tag(tagId, tagUserId, tagName, color));
                }
            }
        }
    }

    /** Replaces all tag associations for the story with the current story.getTagIds() set. */
    private void syncTags(Connection conn, Story story) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM story_tags WHERE story_id = ?")) {
            del.setString(1, story.getId().toString());
            del.executeUpdate();
        }

        if (!story.getTagIds().isEmpty()) {
            String ins = "INSERT INTO story_tags (story_id, tag_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(ins)) {
                for (UUID tagId : story.getTagIds()) {
                    ps.setString(1, story.getId().toString());
                    ps.setString(2, tagId.toString());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private long countRows(Connection conn, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            RepositoryUtils.bindParams(ps, params, 1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }
}
