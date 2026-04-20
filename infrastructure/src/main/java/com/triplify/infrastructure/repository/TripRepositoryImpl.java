package com.triplify.infrastructure.repository;

import com.triplify.domain.filter.TripFilter;
import com.triplify.domain.model.Category;
import com.triplify.domain.model.Country;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.Tag;
import com.triplify.domain.model.Trip;
import com.triplify.domain.model.enums.ColorEnum;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.TripRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TripRepositoryImpl implements TripRepository {

    private static final Logger log = LoggerFactory.getLogger(TripRepositoryImpl.class);

    private static final String TRIP_WITH_CATEGORY_SELECT = """
        SELECT
            t.id AS t_id,
            t.user_id AS t_user_id,
            t.category_id AS t_category_id,
            t.cover_image_id AS t_cover_image_id,
            t.title AS t_title,
            t.description AS t_description,
            t.status AS t_status,
            t.started_at AS t_started_at,
            t.ended_at AS t_ended_at,
            t.created_at AS t_created_at,
            t.updated_at AS t_updated_at,

            i.id AS i_id,
            i.url AS i_url,
            i.description AS i_description,
            i.uploaded_at AS i_uploaded_at,

            c.id AS c_id,
            c.created_by AS c_created_by,
            c.name AS c_name,
            c.name_sk AS c_name_sk,
            c.description AS c_description,
            c.description_sk AS c_description_sk,
            c.emoji_unicode AS c_emoji_unicode,
            c.color AS c_color
        FROM trips t
        LEFT JOIN images i ON t.cover_image_id = i.id
        LEFT JOIN categories c ON t.category_id = c.id
        """;

    @Override
    public Optional<Trip> findById(UUID id) {
        String sql = TRIP_WITH_CATEGORY_SELECT + " WHERE t.id = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Trip trip = mapTrip(rs);
                return Optional.of(hydrateTrips(conn, List.of(trip)).getFirst());
            }
        } catch (SQLException e) {
            log.error("Failed to find trip by id='{}'", id, e);
            throw new RuntimeException("Database error while finding trip by id", e);
        }
    }

    @Override
    public Page<Trip> findList(PageRequest pageRequest, TripFilter filter, boolean startTimeAsc) {
        StringBuilder sql = new StringBuilder(TRIP_WITH_CATEGORY_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (filter != null) {
            if (filter.name() != null && !filter.name().isBlank()) {
                sql.append("AND t.title LIKE ? ");
                params.add(filter.name() + "%");
            }
            if (filter.countryId() != null && !filter.countryId().isBlank()) {
                sql.append("AND EXISTS (SELECT 1 FROM trip_countries tc WHERE tc.trip_id = t.id AND tc.country_id = ?) ");
                params.add(filter.countryId());
            }
            if (filter.status() != null) {
                sql.append("AND t.status = ? ");
                params.add(filter.status().getValue());
            }
            if (filter.categoryId() != null && !filter.categoryId().isBlank()) {
                sql.append("AND t.category_id = ? ");
                params.add(filter.categoryId());
            }
            if (filter.startedFrom() != null) {
                sql.append("AND t.started_at >= ? ");
                params.add(filter.startedFrom().toString());
            }
            if (filter.startedTo() != null) {
                sql.append("AND t.started_at <= ? ");
                params.add(filter.startedTo().toString());
            }
            if (filter.tagIds() != null) {
                for (String tagId : filter.tagIds()) {
                    sql.append("AND EXISTS (SELECT 1 FROM trip_tags tt WHERE tt.trip_id = t.id AND tt.tag_id = ?) ");
                    params.add(tagId);
                }
            }
        }

        sql.append("ORDER BY (t.started_at IS NULL) ASC, t.started_at ")
                .append(startTimeAsc ? "ASC" : "DESC")
                .append(", t.updated_at DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(pageRequest.size() + 1);
        params.add(pageRequest.offset());

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Trip> trips = new ArrayList<>();
                while (rs.next()) {
                    trips.add(mapTrip(rs));
                }

                boolean hasNext = trips.size() > pageRequest.size();
                if (hasNext) {
                    trips.removeLast();
                }

                return Page.of(hydrateTrips(conn, trips), pageRequest, hasNext);
            }
        } catch (SQLException e) {
            log.error("Failed to find trips", e);
            throw new RuntimeException("Database error while finding trips", e);
        }
    }

    @Override
    public void create(Trip trip) {
        String sql = """
            INSERT INTO trips (
                id, user_id, category_id, cover_image_id, title, description,
                status, started_at, ended_at, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trip.getId().toString());
            ps.setString(2, trip.getUserId().toString());
            setNullableUuid(ps, 3, trip.getCategoryId());
            setNullableUuid(ps, 4, trip.getCoverImageId());
            ps.setString(5, trip.getTitle());
            ps.setString(6, trip.getDescription());
            ps.setString(7, trip.getStatus().getValue());
            setNullableInstant(ps, 8, trip.getStartedAt());
            setNullableInstant(ps, 9, trip.getEndedAt());
            ps.setString(10, trip.getCreatedAt().toString());
            ps.setString(11, trip.getUpdatedAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to create trip id='{}'", trip.getId(), e);
            throw new RuntimeException("Database error while creating trip", e);
        }
    }

    @Override
    public void update(Trip trip) {
        String sql = """
            UPDATE trips
            SET user_id = ?, category_id = ?, cover_image_id = ?, title = ?, description = ?, status = ?,
                started_at = ?, ended_at = ?, updated_at = ?
            WHERE id = ?
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trip.getUserId().toString());
            setNullableUuid(ps, 2, trip.getCategoryId());
            setNullableUuid(ps, 3, trip.getCoverImageId());
            ps.setString(4, trip.getTitle());
            ps.setString(5, trip.getDescription());
            ps.setString(6, trip.getStatus().getValue());
            setNullableInstant(ps, 7, trip.getStartedAt());
            setNullableInstant(ps, 8, trip.getEndedAt());
            ps.setString(9, trip.getUpdatedAt().toString());
            ps.setString(10, trip.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update trip id='{}'", trip.getId(), e);
            throw new RuntimeException("Database error while updating trip", e);
        }
    }

    @Override
    public void delete(Trip trip) {
        String sql = "DELETE FROM trips WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trip.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete trip id='{}'", trip.getId(), e);
            throw new RuntimeException("Database error while deleting trip", e);
        }
    }

    @Override
    public void replaceTagIds(UUID tripId, Set<UUID> tagIds) {
        replaceRelationIds("trip_tags", "tag_id", "trip_id", tripId, tagIds);
    }

    @Override
    public void replaceCountryIds(UUID tripId, Set<UUID> countryIds) {
        replaceRelationIds("trip_countries", "country_id", "trip_id", tripId, countryIds);
    }

    @Override
    public void updateCoverImageId(UUID tripId, UUID coverImageId) {
        String sql = "UPDATE trips SET cover_image_id = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (coverImageId == null) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, coverImageId.toString());
            }
            ps.setString(2, Instant.now().toString());
            ps.setString(3, tripId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update cover image for trip id='{}'", tripId, e);
            throw new RuntimeException("Database error while updating trip cover image", e);
        }
    }

    private List<Trip> hydrateTrips(Connection conn, List<Trip> trips) throws SQLException {
        if (trips.isEmpty()) {
            return trips;
        }

        Set<String> tripIds = trips.stream()
                .map(trip -> trip.getId().toString())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, LinkedHashSet<Tag>> tagsByTripId = loadTagsByTripIds(conn, tripIds);
        Map<String, LinkedHashSet<Country>> countriesByTripId = loadCountriesByTripIds(conn, tripIds);

        List<Trip> hydratedTrips = new ArrayList<>(trips.size());
        for (Trip trip : trips) {
            LinkedHashSet<Tag> tags = tagsByTripId.getOrDefault(trip.getId().toString(), new LinkedHashSet<>());
            LinkedHashSet<Country> countries = countriesByTripId.getOrDefault(trip.getId().toString(), new LinkedHashSet<>());
            hydratedTrips.add(new Trip(
                    trip.getId(),
                    trip.getUserId(),
                    trip.getCategoryId(),
                    trip.getCategory(),
                    trip.getCoverImageId(),
                    trip.getCoverImage(),
                    trip.getTitle(),
                    trip.getDescription(),
                    trip.getStatus(),
                    trip.getStartedAt(),
                    trip.getEndedAt(),
                    trip.getCreatedAt(),
                    trip.getUpdatedAt(),
                    tags.stream().map(Tag::getId).collect(Collectors.toCollection(LinkedHashSet::new)),
                    countries.stream().map(Country::getId).collect(Collectors.toCollection(LinkedHashSet::new)),
                    new LinkedHashSet<>(tags),
                    new LinkedHashSet<>(countries)
            ));
        }

        return hydratedTrips;
    }

    private Map<String, LinkedHashSet<Tag>> loadTagsByTripIds(Connection conn, Set<String> tripIds) throws SQLException {
        Map<String, LinkedHashSet<Tag>> result = new HashMap<>();
        if (tripIds.isEmpty()) {
            return result;
        }

        String placeholders = String.join(", ", Collections.nCopies(tripIds.size(), "?"));
        String sql = """
            SELECT
                tt.trip_id AS trip_id,
                tg.id AS tag_id,
                tg.user_id AS tag_user_id,
                tg.name AS tag_name,
                tg.color AS tag_color
            FROM trip_tags tt
            INNER JOIN tags tg ON tg.id = tt.tag_id
            WHERE tt.trip_id IN (%s)
            ORDER BY tt.trip_id, tg.name COLLATE NOCASE ASC, tg.id ASC
            """.formatted(placeholders);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            for (String tripId : tripIds) {
                ps.setString(index++, tripId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.computeIfAbsent(rs.getString("trip_id"), key -> new LinkedHashSet<>())
                            .add(new Tag(
                                    UUID.fromString(rs.getString("tag_id")),
                                    UUID.fromString(rs.getString("tag_user_id")),
                                    rs.getString("tag_name"),
                                    parseColor(rs.getString("tag_color"))
                            ));
                }
            }
        }

        return result;
    }

    private Map<String, LinkedHashSet<Country>> loadCountriesByTripIds(Connection conn, Set<String> tripIds) throws SQLException {
        Map<String, LinkedHashSet<Country>> result = new HashMap<>();
        if (tripIds.isEmpty()) {
            return result;
        }

        String placeholders = String.join(", ", Collections.nCopies(tripIds.size(), "?"));
        String sql = """
            SELECT
                tc.trip_id AS trip_id,
                c.id AS country_id,
                c.created_by AS country_created_by,
                c.name AS country_name,
                c.name_sk AS country_name_sk,
                c.emoji_unicode AS country_emoji_unicode,
                c.is_available AS country_is_available
            FROM trip_countries tc
            INNER JOIN countries c ON c.id = tc.country_id
            WHERE tc.trip_id IN (%s)
            ORDER BY tc.trip_id, c.name COLLATE NOCASE ASC, c.id ASC
            """.formatted(placeholders);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            for (String tripId : tripIds) {
                ps.setString(index++, tripId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String createdByRaw = rs.getString("country_created_by");
                    result.computeIfAbsent(rs.getString("trip_id"), key -> new LinkedHashSet<>())
                            .add(new Country(
                                    UUID.fromString(rs.getString("country_id")),
                                    createdByRaw == null ? null : UUID.fromString(createdByRaw),
                                    rs.getString("country_name"),
                                    rs.getString("country_name_sk"),
                                    rs.getString("country_emoji_unicode"),
                                    rs.getBoolean("country_is_available")
                            ));
                }
            }
        }

        return result;
    }

    private void replaceRelationIds(String tableName, String relationColumn, String ownerColumn, UUID ownerId, Set<UUID> relationIds) {
        String deleteSql = "DELETE FROM %s WHERE %s = ?".formatted(tableName, ownerColumn);
        String insertSql = "INSERT INTO %s (%s, %s) VALUES (?, ?)".formatted(tableName, ownerColumn, relationColumn);

        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            try (PreparedStatement deleteStatement = conn.prepareStatement(deleteSql)) {
                deleteStatement.setString(1, ownerId.toString());
                deleteStatement.executeUpdate();
            }

            if (relationIds == null || relationIds.isEmpty()) {
                return;
            }

            try (PreparedStatement insertStatement = conn.prepareStatement(insertSql)) {
                for (UUID relationId : relationIds) {
                    insertStatement.setString(1, ownerId.toString());
                    insertStatement.setString(2, relationId.toString());
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }
        } catch (SQLException e) {
            log.error("Failed to replace relation ids in table='{}' for ownerId='{}'", tableName, ownerId, e);
            throw new RuntimeException("Database error while replacing trip relation ids", e);
        }
    }

    private Trip mapTrip(ResultSet rs) throws SQLException {
        UUID categoryId = nullableUuid(rs.getString("t_category_id"));
        UUID coverImageId = nullableUuid(rs.getString("t_cover_image_id"));
        Image coverImage = null;
        if (rs.getString("i_id") != null && rs.getString("i_url") != null) {
            coverImage = new Image(
                    UUID.fromString(rs.getString("i_id")),
                    Path.of(rs.getString("i_url")),
                    rs.getString("i_description"),
                    Instant.parse(rs.getString("i_uploaded_at"))
            );
        }

        Category category = null;
        if (rs.getString("c_id") != null) {
            category = new Category(
                    UUID.fromString(rs.getString("c_id")),
                    nullableUuid(rs.getString("c_created_by")),
                    rs.getString("c_name"),
                    rs.getString("c_name_sk"),
                    rs.getString("c_description"),
                    rs.getString("c_description_sk"),
                    rs.getString("c_emoji_unicode"),
                    parseColor(rs.getString("c_color"))
            );
        }

        return new Trip(
                UUID.fromString(rs.getString("t_id")),
                UUID.fromString(rs.getString("t_user_id")),
                categoryId,
                category,
                coverImageId,
                coverImage,
                rs.getString("t_title"),
                rs.getString("t_description"),
                StatusEnum.fromValue(rs.getString("t_status")),
                nullableInstant(rs.getString("t_started_at")),
                nullableInstant(rs.getString("t_ended_at")),
                Instant.parse(rs.getString("t_created_at")),
                Instant.parse(rs.getString("t_updated_at")),
                new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new LinkedHashSet<>(),
                new LinkedHashSet<>()
        );
    }

    private ColorEnum parseColor(String color) {
        if (color == null || color.isBlank()) {
            return ColorEnum.GRAY;
        }
        return ColorEnum.valueOf(color.trim().toUpperCase(Locale.ROOT));
    }

    private UUID nullableUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private Instant nullableInstant(String value) {
        return value == null ? null : Instant.parse(value);
    }

    private void setNullableUuid(PreparedStatement ps, int index, UUID value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.toString());
        }
    }

    private void setNullableInstant(PreparedStatement ps, int index, Instant value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.toString());
        }
    }
}
