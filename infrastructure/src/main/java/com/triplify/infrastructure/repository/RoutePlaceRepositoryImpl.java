package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Country;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.Place;
import com.triplify.domain.model.Route;
import com.triplify.domain.model.RoutePlace;
import com.triplify.domain.model.RouteWithPlaces;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public Optional<RoutePlace> findByRouteIdAndPlaceId(UUID routeId, UUID placeId) {
        String sql = ROUTE_PLACE_WITH_RELATIONS_SELECT + """
             WHERE rp.route_id = ? AND rp.place_id = ?
             ORDER BY rp."order" ASC
             LIMIT 1
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routeId.toString());
            ps.setString(2, placeId.toString());

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
    public List<RoutePlace> findByRouteId(UUID routeId) {
        String sql = ROUTE_PLACE_WITH_RELATIONS_SELECT + """
             WHERE rp.route_id = ?
             ORDER BY rp."order" ASC
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routeId.toString());

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
    public List<RoutePlace> findByPlaceId(UUID placeId) {
        String sql = ROUTE_PLACE_WITH_RELATIONS_SELECT + """
             WHERE rp.place_id = ?
             ORDER BY rp.updated_at DESC, rp.created_at DESC
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, placeId.toString());

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
    public Page<RouteWithPlaces> findRoutesWithPlacesByPlaceId(PageRequest pageRequest, UUID placeId, UUID userId) {
        String sql = """
                WITH selected_routes AS (
                    SELECT
                        rp.route_id AS route_id,
                        MAX(rp.updated_at) AS assoc_updated_at,
                        MAX(rp.created_at) AS assoc_created_at
                    FROM route_places rp
                    INNER JOIN routes r ON r.id = rp.route_id
                    WHERE rp.place_id = ? AND r.user_id = ?
                    GROUP BY rp.route_id
                    ORDER BY assoc_updated_at DESC, assoc_created_at DESC
                    LIMIT ? OFFSET ?
                )
                SELECT
                    sr.route_id AS sr_route_id,
                    sr.assoc_updated_at AS sr_assoc_updated_at,
                    sr.assoc_created_at AS sr_assoc_created_at,

                    r.id AS r_id,
                    r.user_id AS r_user_id,
                    r.cover_image_id AS r_cover_image_id,
                    r.title AS r_title,
                    r.description AS r_description,
                    r.length AS r_length,
                    r.created_at AS r_created_at,
                    r.updated_at AS r_updated_at,

                    ri.id AS ri_id,
                    ri.url AS ri_url,
                    ri.description AS ri_description,
                    ri.uploaded_at AS ri_uploaded_at,

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

                    pi.id AS pi_id,
                    pi.url AS pi_url,
                    pi.description AS pi_description,
                    pi.uploaded_at AS pi_uploaded_at
                FROM selected_routes sr
                INNER JOIN routes r ON r.id = sr.route_id
                LEFT JOIN images ri ON r.cover_image_id = ri.id
                LEFT JOIN route_places rp ON rp.route_id = r.id
                LEFT JOIN places p ON rp.place_id = p.id
                LEFT JOIN countries c ON p.country_id = c.id
                LEFT JOIN images pi ON p.cover_image_id = pi.id
                ORDER BY sr.assoc_updated_at DESC, sr.assoc_created_at DESC, rp."order" ASC
                """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, placeId.toString());
            ps.setString(2, userId.toString());
            ps.setInt(3, pageRequest.size() + 1);
            ps.setInt(4, pageRequest.offset());

            try (ResultSet rs = ps.executeQuery()) {
                Map<UUID, RouteAccumulator> byRouteId = new LinkedHashMap<>();
                while (rs.next()) {
                    UUID routeId = UUID.fromString(rs.getString("r_id"));
                    RouteAccumulator accumulator = byRouteId.get(routeId);
                    if (accumulator == null) {
                        accumulator = new RouteAccumulator(mapRoute(rs));
                        byRouteId.put(routeId, accumulator);
                    }

                    String routePlaceId = rs.getString("rp_id");
                    if (routePlaceId != null) {
                        accumulator.routePlaces().add(mapRoutePlaceFromRow(rs));
                    }
                }

                List<RouteWithPlaces> routes = byRouteId.values().stream()
                        .map(item -> new RouteWithPlaces(item.route(), List.copyOf(item.routePlaces())))
                        .toList();

                boolean hasNext = routes.size() > pageRequest.size();
                if (hasNext) {
                    routes = routes.subList(0, pageRequest.size());
                }
                return Page.of(routes, pageRequest, hasNext);
            }
        } catch (SQLException e) {
            log.error("Error fetching routes for placeId='{}'", placeId, e);
            throw new RuntimeException("Database error while finding routes by place", e);
        }
    }

    private Route mapRoute(ResultSet rs) throws SQLException {
        Image coverImage = null;
        String imageId = rs.getString("ri_id");
        if (imageId != null) {
            coverImage = new Image(
                    UUID.fromString(imageId),
                    Path.of(rs.getString("ri_url")),
                    rs.getString("ri_description"),
                    Instant.parse(rs.getString("ri_uploaded_at"))
            );
        }

        return new Route(
                UUID.fromString(rs.getString("r_id")),
                UUID.fromString(rs.getString("r_user_id")),
                rs.getString("r_cover_image_id") == null ? null : UUID.fromString(rs.getString("r_cover_image_id")),
                coverImage,
                rs.getString("r_title"),
                rs.getString("r_description"),
                rs.getObject("r_length") == null ? null : rs.getDouble("r_length"),
                Instant.parse(rs.getString("r_created_at")),
                Instant.parse(rs.getString("r_updated_at"))
        );
    }

    private RoutePlace mapRoutePlaceFromRow(ResultSet rs) throws SQLException {
        Country country = new Country(
                UUID.fromString(rs.getString("c_id")),
                rs.getString("c_created_by") != null ? UUID.fromString(rs.getString("c_created_by")) : null,
                rs.getString("c_name"),
                rs.getString("c_name_sk"),
                rs.getString("c_emoji_unicode"),
                rs.getBoolean("c_is_available")
        );

        Image coverImage = null;
        String imageId = rs.getString("pi_id");
        if (imageId != null) {
            coverImage = new Image(
                    UUID.fromString(imageId),
                    Path.of(rs.getString("pi_url")),
                    rs.getString("pi_description"),
                    Instant.parse(rs.getString("pi_uploaded_at"))
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

    private record RouteAccumulator(Route route, List<RoutePlace> routePlaces) {
        private RouteAccumulator(Route route) {
            this(route, new ArrayList<>());
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
