package com.triplify.infrastructure.repository;

import com.triplify.domain.filter.RouteFilter;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.Route;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.RouteRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RouteRepositoryImpl implements RouteRepository {

    private static final Logger log = LoggerFactory.getLogger(RouteRepositoryImpl.class);

    private static final String ROUTE_WITH_IMAGE_SELECT = """
        SELECT
            r.id AS r_id,
            r.user_id AS r_user_id,
            r.cover_image_id AS r_cover_image_id,
            r.title AS r_title,
            r.description AS r_description,
            r.length AS r_length,
            r.created_at AS r_created_at,
            r.updated_at AS r_updated_at,

            i.id AS i_id,
            i.url AS i_url,
            i.description AS i_description,
            i.uploaded_at AS i_uploaded_at
        FROM routes r
        LEFT JOIN images i ON r.cover_image_id = i.id
        """;

    @Override
    public Optional<Route> findById(String id) {
        String sql = ROUTE_WITH_IMAGE_SELECT + " WHERE r.id = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRouteWithRelations(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching Route by id='{}'", id, e);
            throw new RuntimeException("Database error while finding route by id", e);
        }

        return Optional.empty();
    }

    @Override
    public Page<Route> findList(PageRequest page, RouteFilter filter) {
        StringBuilder sql = new StringBuilder(ROUTE_WITH_IMAGE_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (filter != null && filter.name() != null && !filter.name().isBlank()) {
            sql.append("AND r.title LIKE ? ");
            params.add(filter.name() + "%");
        }

        sql.append("ORDER BY r.updated_at DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(page.size() + 1);
        params.add(page.offset());

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Route> routes = new ArrayList<>();
                while (rs.next()) {
                    routes.add(mapRouteWithRelations(rs));
                }

                boolean hasNext = routes.size() > page.size();
                if (hasNext) {
                    routes.remove(routes.size() - 1);
                }

                return Page.of(routes, page, hasNext);
            }
        } catch (SQLException e) {
            String name = filter != null ? filter.name() : null;
            log.error("Error finding routes with name='{}'", name, e);
            throw new RuntimeException("Database error while finding routes", e);
        }
    }

    @Override
    public void create(Route route) {
        String sql = """
            INSERT INTO routes (id, user_id, cover_image_id, title, description, length)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, route.getId().toString());
            ps.setString(2, route.getUserID().toString());
            setNullableUUID(ps, 3, route.getCoverImageId());
            ps.setString(4, route.getTitle());
            ps.setString(5, route.getDescription());
            setNullableDouble(ps, 6, route.getLength());
            ps.executeUpdate();

            log.debug("Created Route with id='{}', title='{}'", route.getId(), route.getTitle());
        } catch (SQLException e) {
            log.error("Error creating Route with id='{}'", route.getId(), e);
            throw new RuntimeException("Database error while creating route", e);
        }
    }

    @Override
    public void update(Route route) {
        String sql = """
            UPDATE routes
            SET user_id = ?, cover_image_id = ?, title = ?, description = ?, length = ?
            WHERE id = ?
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, route.getUserID().toString());
            setNullableUUID(ps, 2, route.getCoverImageId());
            ps.setString(3, route.getTitle());
            ps.setString(4, route.getDescription());
            setNullableDouble(ps, 5, route.getLength());
            ps.setString(6, route.getId().toString());
            ps.executeUpdate();

            log.debug("Updated Route with id='{}', title='{}'", route.getId(), route.getTitle());
        } catch (SQLException e) {
            log.error("Error updating Route with id='{}'", route.getId(), e);
            throw new RuntimeException("Database error while updating route", e);
        }
    }

    @Override
    public void delete(Route route) {
        String sql = "DELETE FROM routes WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, route.getId().toString());
            ps.executeUpdate();

            log.debug("Deleted Route with id='{}', title='{}'", route.getId(), route.getTitle());
        } catch (SQLException e) {
            log.error("Error deleting Route with id='{}'", route.getId(), e);
            throw new RuntimeException("Database error while deleting route", e);
        }
    }

    private Route mapRouteWithRelations(ResultSet rs) throws SQLException {
        Image coverImage = null;
        String imageId = rs.getString("i_id");
        if (imageId != null) {
            coverImage = new Image(
                    UUID.fromString(imageId),
                    Path.of(rs.getString("i_url")),
                    rs.getString("i_description"),
                    Instant.parse(rs.getString("i_uploaded_at"))
            );
        }

        return new Route(
                UUID.fromString(rs.getString("r_id")),
                UUID.fromString(rs.getString("r_user_id")),
                nullableUuid(rs.getString("r_cover_image_id")),
                coverImage,
                rs.getString("r_title"),
                rs.getString("r_description"),
                nullableDouble(rs, "r_length"),
                Instant.parse(rs.getString("r_created_at")),
                Instant.parse(rs.getString("r_updated_at"))
        );
    }

    private UUID nullableUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private void setNullableUUID(PreparedStatement ps, int index, UUID value) throws SQLException {
        if (value != null) {
            ps.setString(index, value.toString());
        } else {
            ps.setNull(index, java.sql.Types.VARCHAR);
        }
    }

    private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value != null) {
            ps.setDouble(index, value);
        } else {
            ps.setNull(index, java.sql.Types.DOUBLE);
        }
    }
}
