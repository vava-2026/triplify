package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Country;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.Place;
import com.triplify.domain.model.RoutePlace;
import com.triplify.domain.repository.RoutePlaceRepository;
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

public class RoutePlaceRepositoryImpl implements RoutePlaceRepository {

    private static final Logger log = LoggerFactory.getLogger(RoutePlaceRepositoryImpl.class);

    private static final String ROUTE_PLACE_WITH_RELATIONS_SELECT = """
        SELECT
            rp.id AS rp_id,
            rp.route_id AS rp_route_id,
            rp.place_id AS rp_place_id,
            rp."order" AS rp_order,
            rp.created_at AS rp_created_at,
            rp.updated_at AS rp_updated_at,

            p.id AS p_id,
            p.user_id AS p_user_id,
            p.country_id AS p_country_id,
            p.cover_image_id AS p_cover_image_id,
            p.title AS p_title,
            p.description AS p_description,
            p.latitude AS p_latitude,
            p.longitude AS p_longitude,
            p.created_at AS p_created_at,
            p.updated_at AS p_updated_at,

            c.id AS c_id,
            c.created_by AS c_created_by,
            c.name AS c_name,
            c.name_sk AS c_name_sk,
            c.emoji_unicode AS c_emoji_unicode,
            c.is_available AS c_is_available,

            i.id AS i_id,
            i.url AS i_url,
            i.description AS i_description,
            i.uploaded_at AS i_uploaded_at
        FROM route_places rp
        INNER JOIN places p ON rp.place_id = p.id
        INNER JOIN countries c ON p.country_id = c.id
        LEFT JOIN images i ON p.cover_image_id = i.id
        """;

    @Override
    public Optional<RoutePlace> findByRouteIdAndPlaceId(String routeId, String placeId) {
        String sql = ROUTE_PLACE_WITH_RELATIONS_SELECT + """
             WHERE rp.route_id = ? AND rp.place_id = ?
             ORDER BY rp."order" ASC
             LIMIT 1
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routeId);
            ps.setString(2, placeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRoutePlaceWithRelations(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching RoutePlace for routeId='{}', placeId='{}'", routeId, placeId, e);
            throw new RuntimeException("Database error while finding route place", e);
        }

        return Optional.empty();
    }

    @Override
    public List<RoutePlace> findByRouteId(String routeId) {
        String sql = ROUTE_PLACE_WITH_RELATIONS_SELECT + """
             WHERE rp.route_id = ?
             ORDER BY rp."order" ASC
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routeId);

            try (ResultSet rs = ps.executeQuery()) {
                List<RoutePlace> routePlaces = new ArrayList<>();
                while (rs.next()) {
                    routePlaces.add(mapRoutePlaceWithRelations(rs));
                }
                return routePlaces;
            }
        } catch (SQLException e) {
            log.error("Error fetching RoutePlaces for routeId='{}'", routeId, e);
            throw new RuntimeException("Database error while finding route places", e);
        }
    }

    @Override
    public List<RoutePlace> findByPlaceId(String placeId) {
        String sql = ROUTE_PLACE_WITH_RELATIONS_SELECT + """
             WHERE rp.place_id = ?
             ORDER BY rp.updated_at DESC, rp.created_at DESC
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, placeId);

            try (ResultSet rs = ps.executeQuery()) {
                List<RoutePlace> routePlaces = new ArrayList<>();
                while (rs.next()) {
                    routePlaces.add(mapRoutePlaceWithRelations(rs));
                }
                return routePlaces;
            }
        } catch (SQLException e) {
            log.error("Error fetching RoutePlaces for placeId='{}'", placeId, e);
            throw new RuntimeException("Database error while finding route places by place", e);
        }
    }

    @Override
    public void create(RoutePlace routePlace) {
        String sql = """
            INSERT INTO route_places (id, route_id, place_id, "order")
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routePlace.getId().toString());
            ps.setString(2, routePlace.getRouteId().toString());
            ps.setString(3, routePlace.getPlaceId().toString());
            ps.setInt(4, routePlace.getOrder());
            ps.executeUpdate();

            log.debug("Created RoutePlace with id='{}', routeId='{}', placeId='{}'", routePlace.getId(), routePlace.getRouteId(), routePlace.getPlaceId());
        } catch (SQLException e) {
            log.error("Error creating RoutePlace with id='{}'", routePlace.getId(), e);
            throw new RuntimeException("Database error while creating route place", e);
        }
    }

    @Override
    public void update(RoutePlace routePlace) {
        String sql = """
            UPDATE route_places
            SET route_id = ?, place_id = ?, "order" = ?
            WHERE id = ?
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routePlace.getRouteId().toString());
            ps.setString(2, routePlace.getPlaceId().toString());
            ps.setInt(3, routePlace.getOrder());
            ps.setString(4, routePlace.getId().toString());
            ps.executeUpdate();

            log.debug("Updated RoutePlace with id='{}', order='{}'", routePlace.getId(), routePlace.getOrder());
        } catch (SQLException e) {
            log.error("Error updating RoutePlace with id='{}'", routePlace.getId(), e);
            throw new RuntimeException("Database error while updating route place", e);
        }
    }

    @Override
    public void delete(RoutePlace routePlace) {
        String sql = "DELETE FROM route_places WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routePlace.getId().toString());
            ps.executeUpdate();

            log.debug("Deleted RoutePlace with id='{}'", routePlace.getId());
        } catch (SQLException e) {
            log.error("Error deleting RoutePlace with id='{}'", routePlace.getId(), e);
            throw new RuntimeException("Database error while deleting route place", e);
        }
    }

    private RoutePlace mapRoutePlaceWithRelations(ResultSet rs) throws SQLException {
        Country country = new Country(
                UUID.fromString(rs.getString("c_id")),
                rs.getString("c_created_by") != null ? UUID.fromString(rs.getString("c_created_by")) : null,
                rs.getString("c_name"),
                rs.getString("c_name_sk"),
                rs.getString("c_emoji_unicode"),
                rs.getBoolean("c_is_available")
        );

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

        Place place = new Place(
                UUID.fromString(rs.getString("p_id")),
                UUID.fromString(rs.getString("p_user_id")),
                UUID.fromString(rs.getString("p_country_id")),
                country,
                rs.getString("p_cover_image_id") != null ? UUID.fromString(rs.getString("p_cover_image_id")) : null,
                coverImage,
                rs.getString("p_title"),
                rs.getString("p_description"),
                rs.getDouble("p_latitude"),
                rs.getDouble("p_longitude"),
                Instant.parse(rs.getString("p_created_at")),
                Instant.parse(rs.getString("p_updated_at"))
        );

        return new RoutePlace(
                UUID.fromString(rs.getString("rp_id")),
                UUID.fromString(rs.getString("rp_route_id")),
                UUID.fromString(rs.getString("rp_place_id")),
                place,
                rs.getInt("rp_order"),
                Instant.parse(rs.getString("rp_created_at")),
                Instant.parse(rs.getString("rp_updated_at"))
        );
    }
}
