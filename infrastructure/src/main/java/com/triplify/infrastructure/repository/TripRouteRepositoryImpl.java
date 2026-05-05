package com.triplify.infrastructure.repository;

import com.triplify.domain.model.TripRoute;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.TripRouteRepository;
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

public class TripRouteRepositoryImpl implements TripRouteRepository {

    private static final Logger log = LoggerFactory.getLogger(TripRouteRepositoryImpl.class);

    @Override
    public Optional<TripRoute> findById(UUID id) {
        String sql = """
            SELECT id, trip_id, route_id, "order", status, started_at, ended_at, created_at, updated_at
            FROM trip_routes
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
            log.error("Failed to find trip route by id='{}'", id, e);
            throw new RuntimeException("Database error while finding trip route", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TripRoute> findByTripIdAndRouteId(UUID tripId, UUID routeId) {
        String sql = """
            SELECT id, trip_id, route_id, "order", status, started_at, ended_at, created_at, updated_at
            FROM trip_routes
            WHERE trip_id = ? AND route_id = ?
            LIMIT 1
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tripId.toString());
            ps.setString(2, routeId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find trip route by tripId='{}' and routeId='{}'", tripId, routeId, e);
            throw new RuntimeException("Database error while finding trip route", e);
        }

        return Optional.empty();
    }

    @Override
    public Page<TripRoute> findList(PageRequest pageRequest, UUID tripId, StatusEnum status) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, trip_id, route_id, "order", status, started_at, ended_at, created_at, updated_at
            FROM trip_routes
            WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();

        if (tripId != null) {
            sql.append(" AND trip_id = ? ");
            params.add(tripId.toString());
        }
        if (status != null) {
            sql.append(" AND status = ? ");
            params.add(status.getValue());
        }

        sql.append(" ORDER BY \"order\" ASC, created_at ASC LIMIT ? OFFSET ?");
        params.add(pageRequest.size() + 1);
        params.add(pageRequest.offset());

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<TripRoute> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapRow(rs));
                }

                boolean hasNext = items.size() > pageRequest.size();
                if (hasNext) {
                    items.remove(items.size() - 1);
                }
                return Page.of(items, pageRequest, hasNext);
            }
        } catch (SQLException e) {
            log.error("Failed to query trip routes", e);
            throw new RuntimeException("Database error while querying trip routes", e);
        }
    }

    @Override
    public void create(TripRoute tripRoute) {
        String sql = """
            INSERT INTO trip_routes (id, trip_id, route_id, "order", status, started_at, ended_at, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tripRoute.getId().toString());
            ps.setString(2, tripRoute.getTripId().toString());
            ps.setString(3, tripRoute.getRouteId().toString());
            ps.setInt(4, tripRoute.getOrder());
            ps.setString(5, tripRoute.getStatus().getValue());
            setNullableInstant(ps, 6, tripRoute.getStartedAt());
            setNullableInstant(ps, 7, tripRoute.getEndedAt());
            ps.setString(8, tripRoute.getCreatedAt().toString());
            ps.setString(9, tripRoute.getUpdatedAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to create trip route id='{}'", tripRoute.getId(), e);
            throw new RuntimeException("Database error while creating trip route", e);
        }
    }

    @Override
    public void update(TripRoute tripRoute) {
        String sql = """
            UPDATE trip_routes
            SET trip_id = ?, route_id = ?, "order" = ?, status = ?, started_at = ?, ended_at = ?, updated_at = ?
            WHERE id = ?
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tripRoute.getTripId().toString());
            ps.setString(2, tripRoute.getRouteId().toString());
            ps.setInt(3, tripRoute.getOrder());
            ps.setString(4, tripRoute.getStatus().getValue());
            setNullableInstant(ps, 5, tripRoute.getStartedAt());
            setNullableInstant(ps, 6, tripRoute.getEndedAt());
            ps.setString(7, tripRoute.getUpdatedAt().toString());
            ps.setString(8, tripRoute.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update trip route id='{}'", tripRoute.getId(), e);
            throw new RuntimeException("Database error while updating trip route", e);
        }
    }

    @Override
    public void delete(TripRoute tripRoute) {
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM trip_routes WHERE id = ?")) {
            ps.setString(1, tripRoute.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete trip route id='{}'", tripRoute.getId(), e);
            throw new RuntimeException("Database error while deleting trip route", e);
        }
    }

    private TripRoute mapRow(ResultSet rs) throws SQLException {
        return new TripRoute(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("trip_id")),
                UUID.fromString(rs.getString("route_id")),
                null,
                rs.getInt("order"),
                StatusEnum.fromValue(rs.getString("status")),
                nullableInstant(rs.getString("started_at")),
                nullableInstant(rs.getString("ended_at")),
                Instant.parse(rs.getString("created_at")),
                Instant.parse(rs.getString("updated_at"))
        );
    }

    private Instant nullableInstant(String value) {
        return value == null ? null : Instant.parse(value);
    }

    private void setNullableInstant(PreparedStatement ps, int index, Instant value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.toString());
        }
    }
}
