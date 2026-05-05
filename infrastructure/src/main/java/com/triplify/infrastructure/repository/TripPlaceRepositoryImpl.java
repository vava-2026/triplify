package com.triplify.infrastructure.repository;

import com.triplify.domain.model.TripPlace;
import com.triplify.domain.model.Trip;
import com.triplify.domain.model.Category;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.TripPlaceRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import com.triplify.infrastructure.repository.utils.RepositoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TripPlaceRepositoryImpl implements TripPlaceRepository {

    private static final Logger log = LoggerFactory.getLogger(TripPlaceRepositoryImpl.class);
    private static final String BASE_COLUMNS = "id, trip_id, place_id, visit_date, created_at, updated_at";

    @Override
    public Optional<TripPlace> findById(UUID id) {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            ColumnState columns = tripPlaceColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement(selectSql(columns, " WHERE id = ? LIMIT 1"))) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs, columns));
                }
            }
            }
        } catch (SQLException e) {
            log.error("Failed to find trip place by id='{}'", id, e);
            throw new RuntimeException("Database error while finding trip place", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TripPlace> findByTripIdAndPlaceId(UUID tripId, UUID placeId) {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            ColumnState columns = tripPlaceColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement(selectSql(columns, " WHERE trip_id = ? AND place_id = ? LIMIT 1"))) {
            ps.setString(1, tripId.toString());
            ps.setString(2, placeId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs, columns));
                }
            }
            }
        } catch (SQLException e) {
            log.error("Failed to find trip place by tripId='{}' and placeId='{}'", tripId, placeId, e);
            throw new RuntimeException("Database error while finding trip place", e);
        }

        return Optional.empty();
    }

    @Override
    public List<TripPlace> findByPlaceId(UUID placeId) {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            ColumnState columns = tripPlaceColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement(selectSql(columns, " WHERE place_id = ? ORDER BY created_at DESC"))) {
            ps.setString(1, placeId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<TripPlace> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapRow(rs, columns));
                }
                return items;
            }
            }
        } catch (SQLException e) {
            log.error("Failed to find trip places by placeId='{}'", placeId, e);
            throw new RuntimeException("Database error while finding trip places by place id", e);
        }
    }

    @Override
    public Page<Trip> findTripsByPlaceId(PageRequest pageRequest, UUID placeId, UUID userId) {
        String sql = """
                WITH selected_trips AS (
                    SELECT
                        tp.trip_id AS trip_id,
                        MAX(tp.updated_at) AS assoc_updated_at,
                        MAX(tp.created_at) AS assoc_created_at
                    FROM trip_places tp
                    INNER JOIN trips t ON t.id = tp.trip_id
                    WHERE tp.place_id = ? AND t.user_id = ?
                    GROUP BY tp.trip_id
                    ORDER BY assoc_updated_at DESC, assoc_created_at DESC
                    LIMIT ? OFFSET ?
                )
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
                    c.color AS c_color,
                    st.assoc_updated_at AS assoc_updated_at,
                    st.assoc_created_at AS assoc_created_at
                FROM selected_trips st
                INNER JOIN trips t ON t.id = st.trip_id
                LEFT JOIN images i ON t.cover_image_id = i.id
                LEFT JOIN categories c ON t.category_id = c.id
                ORDER BY st.assoc_updated_at DESC, st.assoc_created_at DESC
                """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, placeId.toString());
            ps.setString(2, userId.toString());
            ps.setInt(3, pageRequest.size() + 1);
            ps.setInt(4, pageRequest.offset());

            try (ResultSet rs = ps.executeQuery()) {
                List<Trip> trips = new ArrayList<>();
                while (rs.next()) {
                    trips.add(mapTripRow(rs));
                }

                boolean hasNext = trips.size() > pageRequest.size();
                if (hasNext) {
                    trips.remove(trips.size() - 1);
                }
                return Page.of(trips, pageRequest, hasNext);
            }
        } catch (SQLException e) {
            log.error("Failed to find trips by placeId='{}'", placeId, e);
            throw new RuntimeException("Database error while finding trips by place id", e);
        }
    }

    private Trip mapTripRow(ResultSet rs) throws SQLException {
        UUID categoryId = nullableUuid(rs.getString("t_category_id"));
        UUID coverImageId = nullableUuid(rs.getString("t_cover_image_id"));

        Image coverImage = null;
        if (rs.getString("i_id") != null && rs.getString("i_url") != null) {
            coverImage = new Image(
                    UUID.fromString(rs.getString("i_id")),
                    java.nio.file.Path.of(rs.getString("i_url")),
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
                    RepositoryUtils.sqlToColor(rs.getString("c_color"))
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

    @Override
    public Page<TripPlace> findList(
            PageRequest pageRequest,
            UUID tripId,
            TripPlaceSourceType sourceType,
            UUID tripRouteId,
            UUID routePlaceId,
            Instant visitFrom,
            Instant visitTo,
            boolean visitTimeAsc
    ) {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            ColumnState columns = tripPlaceColumns(conn);
            String sql = buildFindListSql(columns, tripId, sourceType, tripRouteId, routePlaceId, visitFrom, visitTo, visitTimeAsc);
            List<Object> params = buildFindListParams(columns, tripId, sourceType, tripRouteId, routePlaceId, visitFrom, visitTo, pageRequest);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindParams(ps, params);

                try (ResultSet rs = ps.executeQuery()) {
                    List<TripPlace> items = new ArrayList<>();
                    while (rs.next()) {
                        items.add(mapRow(rs, columns));
                    }

                    boolean hasNext = items.size() > pageRequest.size();
                    if (hasNext) {
                        items.remove(items.size() - 1);
                    }
                    return Page.of(items, pageRequest, hasNext);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query trip places", e);
            throw new RuntimeException("Database error while querying trip places", e);
        }
    }

    @Override
    public void create(TripPlace tripPlace) {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            ColumnState columns = tripPlaceColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement(insertSql(columns))) {
            ps.setString(1, tripPlace.getId().toString());
            ps.setString(2, tripPlace.getTripId().toString());
            ps.setString(3, tripPlace.getPlaceId().toString());

            int index = 4;
            if (columns.hasSourceType()) {
                ps.setString(index++, tripPlace.getSourceType().getValue());
            }
            if (columns.hasTripRouteId()) {
                setNullableUuid(ps, index++, tripPlace.getTripRouteId());
            }
            if (columns.hasRoutePlaceId()) {
                setNullableUuid(ps, index++, tripPlace.getRoutePlaceId());
            }

            setNullableInstant(ps, index++, tripPlace.getVisitDate());
            ps.setString(index++, tripPlace.getStatus().getValue());
            ps.setString(index++, tripPlace.getCreatedAt().toString());
            ps.setString(index, tripPlace.getUpdatedAt().toString());
            ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Failed to create trip place id='{}'", tripPlace.getId(), e);
            throw new RuntimeException("Database error while creating trip place", e);
        }
    }

    @Override
    public void update(TripPlace tripPlace) {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            ColumnState columns = tripPlaceColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement(updateSql(columns))) {
            ps.setString(1, tripPlace.getTripId().toString());
            ps.setString(2, tripPlace.getPlaceId().toString());

            int index = 3;
            if (columns.hasSourceType()) {
                ps.setString(index++, tripPlace.getSourceType().getValue());
            }
            if (columns.hasTripRouteId()) {
                setNullableUuid(ps, index++, tripPlace.getTripRouteId());
            }
            if (columns.hasRoutePlaceId()) {
                setNullableUuid(ps, index++, tripPlace.getRoutePlaceId());
            }

            setNullableInstant(ps, index++, tripPlace.getVisitDate());
            ps.setString(index++, tripPlace.getStatus().getValue());
            ps.setString(index++, tripPlace.getUpdatedAt().toString());
            ps.setString(index, tripPlace.getId().toString());
            ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Failed to update trip place id='{}'", tripPlace.getId(), e);
            throw new RuntimeException("Database error while updating trip place", e);
        }
    }

    @Override
    public void delete(TripPlace tripPlace) {
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM trip_places WHERE id = ?")) {
            ps.setString(1, tripPlace.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete trip place id='{}'", tripPlace.getId(), e);
            throw new RuntimeException("Database error while deleting trip place", e);
        }
    }

    private TripPlace mapRow(ResultSet rs, ColumnState columns) throws SQLException {
        return new TripPlace(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("trip_id")),
                UUID.fromString(rs.getString("place_id")),
                null,
                columns.hasSourceType() ? TripPlaceSourceType.fromValue(rs.getString("source_type")) : TripPlaceSourceType.MANUAL,
                columns.hasTripRouteId() ? nullableUuid(rs.getString("trip_route_id")) : null,
                columns.hasRoutePlaceId() ? nullableUuid(rs.getString("route_place_id")) : null,
                nullableInstant(rs.getString("visit_date")),
                StatusEnum.fromValue(rs.getString("status")),
                Instant.parse(rs.getString("created_at")),
                Instant.parse(rs.getString("updated_at"))
        );
    }

    private String selectSql(ColumnState columns, String suffix) {
        StringBuilder sql = new StringBuilder("SELECT ").append(BASE_COLUMNS);
        if (columns.hasSourceType()) {
            sql.append(", source_type");
        }
        if (columns.hasTripRouteId()) {
            sql.append(", trip_route_id");
        }
        if (columns.hasRoutePlaceId()) {
            sql.append(", route_place_id");
        }
        sql.append(", status");
        sql.append(" FROM trip_places").append(suffix);
        return sql.toString();
    }

    private String buildFindListSql(
            ColumnState columns,
            UUID tripId,
            TripPlaceSourceType sourceType,
            UUID tripRouteId,
            UUID routePlaceId,
            Instant visitFrom,
            Instant visitTo,
            boolean visitTimeAsc
    ) {
        StringBuilder sql = new StringBuilder(selectSql(columns, " WHERE 1=1"));

        if (tripId != null) {
            sql.append(" AND trip_id = ? ");
        }
        if (columns.hasSourceType() && sourceType != null) {
            sql.append(" AND source_type = ? ");
        }
        if (columns.hasTripRouteId() && tripRouteId != null) {
            sql.append(" AND trip_route_id = ? ");
        }
        if (columns.hasRoutePlaceId() && routePlaceId != null) {
            sql.append(" AND route_place_id = ? ");
        }
        if (visitFrom != null) {
            sql.append(" AND visit_date >= ? ");
        }
        if (visitTo != null) {
            sql.append(" AND visit_date <= ? ");
        }

        sql.append(" ORDER BY (visit_date IS NULL) ASC, visit_date ")
                .append(visitTimeAsc ? "ASC" : "DESC")
                .append(", created_at ASC LIMIT ? OFFSET ?");
        return sql.toString();
    }

    private List<Object> buildFindListParams(
            ColumnState columns,
            UUID tripId,
            TripPlaceSourceType sourceType,
            UUID tripRouteId,
            UUID routePlaceId,
            Instant visitFrom,
            Instant visitTo,
            PageRequest pageRequest
    ) {
        List<Object> params = new ArrayList<>();

        if (tripId != null) {
            params.add(tripId.toString());
        }
        if (columns.hasSourceType() && sourceType != null) {
            params.add(sourceType.getValue());
        }
        if (columns.hasTripRouteId() && tripRouteId != null) {
            params.add(tripRouteId.toString());
        }
        if (columns.hasRoutePlaceId() && routePlaceId != null) {
            params.add(routePlaceId.toString());
        }
        if (visitFrom != null) {
            params.add(visitFrom.toString());
        }
        if (visitTo != null) {
            params.add(visitTo.toString());
        }

        params.add(pageRequest.size() + 1);
        params.add(pageRequest.offset());
        return params;
    }

    private String insertSql(ColumnState columns) {
        StringBuilder cols = new StringBuilder("id, trip_id, place_id");
        StringBuilder values = new StringBuilder("?, ?, ?");
        if (columns.hasSourceType()) {
            cols.append(", source_type");
            values.append(", ?");
        }
        if (columns.hasTripRouteId()) {
            cols.append(", trip_route_id");
            values.append(", ?");
        }
        if (columns.hasRoutePlaceId()) {
            cols.append(", route_place_id");
            values.append(", ?");
        }
        cols.append(", visit_date");
        values.append(", ?");
        cols.append(", status");
        values.append(", ?");
        cols.append(", created_at, updated_at");
        values.append(", ?, ?");
        return "INSERT INTO trip_places (" + cols + ") VALUES (" + values + ")";
    }

    private String updateSql(ColumnState columns) {
        StringBuilder sql = new StringBuilder("UPDATE trip_places SET trip_id = ?, place_id = ?");
        if (columns.hasSourceType()) {
            sql.append(", source_type = ?");
        }
        if (columns.hasTripRouteId()) {
            sql.append(", trip_route_id = ?");
        }
        if (columns.hasRoutePlaceId()) {
            sql.append(", route_place_id = ?");
        }
        sql.append(", visit_date = ?");
        sql.append(", status = ?");
        sql.append(", updated_at = ? WHERE id = ?");
        return sql.toString();
    }

    private ColumnState tripPlaceColumns(Connection conn) throws SQLException {
        boolean hasSourceType = false;
        boolean hasTripRouteId = false;
        boolean hasRoutePlaceId = false;

        try (PreparedStatement ps = conn.prepareStatement("PRAGMA table_info(trip_places)");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                if ("source_type".equalsIgnoreCase(name)) {
                    hasSourceType = true;
                } else if ("trip_route_id".equalsIgnoreCase(name)) {
                    hasTripRouteId = true;
                } else if ("route_place_id".equalsIgnoreCase(name)) {
                    hasRoutePlaceId = true;
                }
            }
        }

        return new ColumnState(hasSourceType, hasTripRouteId, hasRoutePlaceId);
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
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

    private record ColumnState(boolean hasSourceType, boolean hasTripRouteId, boolean hasRoutePlaceId) {
    }
}
