package com.triplify.infrastructure.repository;

import com.triplify.domain.model.TripPlace;
import com.triplify.domain.model.enums.TripPlaceSourceType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.TripPlaceRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TripPlaceRepositoryImpl implements TripPlaceRepository {

    private static final Logger log = LoggerFactory.getLogger(TripPlaceRepositoryImpl.class);
    private static final String BASE_COLUMNS = "id, trip_id, place_id, visit_date, created_at, updated_at";

    @Override
    public Optional<TripPlace> findById(String id) {
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql(conn, " WHERE id = ? LIMIT 1"))) {
            ColumnState columns = tripPlaceColumns(conn);
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs, columns));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find trip place by id='{}'", id, e);
            throw new RuntimeException("Database error while finding trip place", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TripPlace> findByTripIdAndPlaceId(String tripId, String placeId) {
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql(conn, " WHERE trip_id = ? AND place_id = ? LIMIT 1"))) {
            ColumnState columns = tripPlaceColumns(conn);
            ps.setString(1, tripId);
            ps.setString(2, placeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs, columns));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find trip place by tripId='{}' and placeId='{}'", tripId, placeId, e);
            throw new RuntimeException("Database error while finding trip place", e);
        }

        return Optional.empty();
    }

    @Override
    public Page<TripPlace> findList(
            PageRequest pageRequest,
            String tripId,
            TripPlaceSourceType sourceType,
            String tripRouteId,
            String routePlaceId,
            Instant visitFrom,
            Instant visitTo,
            boolean visitTimeAsc
    ) {
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildFindListSql(
                     conn, tripId, sourceType, tripRouteId, routePlaceId, visitFrom, visitTo, visitTimeAsc
             ))) {
            ColumnState columns = tripPlaceColumns(conn);
            List<Object> params = buildFindListParams(conn, tripId, sourceType, tripRouteId, routePlaceId, visitFrom, visitTo, pageRequest);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

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
        } catch (SQLException e) {
            log.error("Failed to query trip places", e);
            throw new RuntimeException("Database error while querying trip places", e);
        }
    }

    @Override
    public void create(TripPlace tripPlace) {
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql(conn))) {
            ColumnState columns = tripPlaceColumns(conn);
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
            ps.setString(index++, tripPlace.getCreatedAt().toString());
            ps.setString(index, tripPlace.getUpdatedAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to create trip place id='{}'", tripPlace.getId(), e);
            throw new RuntimeException("Database error while creating trip place", e);
        }
    }

    @Override
    public void update(TripPlace tripPlace) {
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql(conn))) {
            ColumnState columns = tripPlaceColumns(conn);
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
            ps.setString(index++, tripPlace.getUpdatedAt().toString());
            ps.setString(index, tripPlace.getId().toString());
            ps.executeUpdate();
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
                Instant.parse(rs.getString("created_at")),
                Instant.parse(rs.getString("updated_at"))
        );
    }

    private String selectSql(Connection conn, String suffix) throws SQLException {
        ColumnState columns = tripPlaceColumns(conn);
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
        sql.append(" FROM trip_places").append(suffix);
        return sql.toString();
    }

    private String buildFindListSql(
            Connection conn,
            String tripId,
            TripPlaceSourceType sourceType,
            String tripRouteId,
            String routePlaceId,
            Instant visitFrom,
            Instant visitTo,
            boolean visitTimeAsc
    ) throws SQLException {
        ColumnState columns = tripPlaceColumns(conn);
        StringBuilder sql = new StringBuilder(selectSql(conn, " WHERE 1=1"));

        if (tripId != null && !tripId.isBlank()) {
            sql.append(" AND trip_id = ? ");
        }
        if (columns.hasSourceType() && sourceType != null) {
            sql.append(" AND source_type = ? ");
        }
        if (columns.hasTripRouteId() && tripRouteId != null && !tripRouteId.isBlank()) {
            sql.append(" AND trip_route_id = ? ");
        }
        if (columns.hasRoutePlaceId() && routePlaceId != null && !routePlaceId.isBlank()) {
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
            Connection conn,
            String tripId,
            TripPlaceSourceType sourceType,
            String tripRouteId,
            String routePlaceId,
            Instant visitFrom,
            Instant visitTo,
            PageRequest pageRequest
    ) throws SQLException {
        ColumnState columns = tripPlaceColumns(conn);
        List<Object> params = new ArrayList<>();

        if (tripId != null && !tripId.isBlank()) {
            params.add(tripId);
        }
        if (columns.hasSourceType() && sourceType != null) {
            params.add(sourceType.getValue());
        }
        if (columns.hasTripRouteId() && tripRouteId != null && !tripRouteId.isBlank()) {
            params.add(tripRouteId);
        }
        if (columns.hasRoutePlaceId() && routePlaceId != null && !routePlaceId.isBlank()) {
            params.add(routePlaceId);
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

    private String insertSql(Connection conn) throws SQLException {
        ColumnState columns = tripPlaceColumns(conn);
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
        cols.append(", visit_date, created_at, updated_at");
        values.append(", ?, ?, ?");
        return "INSERT INTO trip_places (" + cols + ") VALUES (" + values + ")";
    }

    private String updateSql(Connection conn) throws SQLException {
        ColumnState columns = tripPlaceColumns(conn);
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
        sql.append(", visit_date = ?, updated_at = ? WHERE id = ?");
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
