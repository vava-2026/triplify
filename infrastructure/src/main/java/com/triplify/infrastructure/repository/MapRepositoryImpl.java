package com.triplify.infrastructure.repository;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.triplify.domain.map.MapDataPoint;
import com.triplify.domain.map.MapObjectType;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.MapRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;

public class MapRepositoryImpl implements MapRepository {

    private static final Logger log = LoggerFactory.getLogger(MapRepositoryImpl.class);

    @com.google.inject.Inject
    public MapRepositoryImpl() {
    }

    // Sub-query fragments conditionally included based on filter
    private static final String PLACES_SUB = """
            SELECT p.id, 'PLACE' AS object_type, p.title,
                   p.latitude, p.longitude,
                   i.id AS cover_image_id, i.url AS cover_image_url
            FROM places p
            LEFT JOIN images i ON i.id = p.cover_image_id
            WHERE p.user_id = ?
              AND MbrIntersects(p.geom, BuildMbr(?, ?, ?, ?))
            """;

    private static final String ROUTES_SUB = """
            SELECT r.id, 'ROUTE' AS object_type, r.title,
                   AVG(p.latitude) AS latitude, AVG(p.longitude) AS longitude,
                   i.id AS cover_image_id, i.url AS cover_image_url
            FROM routes r
            JOIN route_places rp ON rp.route_id = r.id
            JOIN places p ON p.id = rp.place_id
            LEFT JOIN images i ON i.id = r.cover_image_id
            WHERE r.user_id = ?
            GROUP BY r.id
            HAVING AVG(p.latitude)  BETWEEN ? AND ?
               AND AVG(p.longitude) BETWEEN ? AND ?
            """;

    // Stories: resolve coords via TripPlace > TripRoute centroid > Trip centroid
    private static final String STORIES_SUB = """
            SELECT s.id, 'STORY' AS object_type, s.title,
                   COALESCE(tp_pl.latitude,  tr_c.lat,  trip_c.lat)  AS latitude,
                   COALESCE(tp_pl.longitude, tr_c.lon,  trip_c.lon) AS longitude,
                   si.id AS cover_image_id, si.url AS cover_image_url
            FROM stories s
            LEFT JOIN trip_places   tp      ON tp.id    = s.trip_place_id
            LEFT JOIN places        tp_pl   ON tp_pl.id = tp.place_id
            LEFT JOIN trip_routes   ttr     ON ttr.id   = s.trip_route_id
            LEFT JOIN (
                SELECT rp.route_id, AVG(p.latitude) lat, AVG(p.longitude) lon
                FROM route_places rp
                JOIN places p ON p.id = rp.place_id
                GROUP BY rp.route_id
            ) tr_c ON tr_c.route_id = ttr.route_id
            LEFT JOIN (
                SELECT tp2.trip_id, AVG(p.latitude) lat, AVG(p.longitude) lon
                FROM trip_places tp2
                JOIN places p ON p.id = tp2.place_id
                GROUP BY tp2.trip_id
            ) trip_c ON trip_c.trip_id = s.trip_id
            LEFT JOIN images si ON si.id = (
                SELECT image_id FROM story_images
                WHERE story_id = s.id
                ORDER BY image_id
                LIMIT 1
            )
            WHERE s.user_id = ?
              AND COALESCE(tp_pl.latitude,  tr_c.lat,  trip_c.lat)  IS NOT NULL
              AND COALESCE(tp_pl.latitude,  tr_c.lat,  trip_c.lat)  BETWEEN ? AND ?
              AND COALESCE(tp_pl.longitude, tr_c.lon,  trip_c.lon) BETWEEN ? AND ?
            """;

    @Override
    public synchronized List<MapDataPoint> getMapMarkers(
            UUID userId,
            double minLatitude, double minLongitude,
            double maxLatitude, double maxLongitude,
            double gridSize,
            Set<MapObjectType> filter) {

        boolean all = filter.isEmpty() || filter.contains(MapObjectType.ALL);
        boolean places = all || filter.contains(MapObjectType.PLACE);
        boolean routes = all || filter.contains(MapObjectType.ROUTE);
        boolean stories = all || filter.contains(MapObjectType.STORY);

        List<String> subQueries = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (places) {
            subQueries.add(PLACES_SUB);
            params.add(userId.toString());
            // BuildMbr expects (minX=minLon, minY=minLat, maxX=maxLon, maxY=maxLat)
            params.add(minLongitude);
            params.add(minLatitude);
            params.add(maxLongitude);
            params.add(maxLatitude);
        }
        if (routes) {
            subQueries.add(ROUTES_SUB);
            params.add(userId.toString());
            params.add(minLatitude);
            params.add(maxLatitude);
            params.add(minLongitude);
            params.add(maxLongitude);
        }
        if (stories) {
            subQueries.add(STORIES_SUB);
            params.add(userId.toString());
            params.add(minLatitude);
            params.add(maxLatitude);
            params.add(minLongitude);
            params.add(maxLongitude);
        }

        if (subQueries.isEmpty()) return List.of();

        String union = String.join(" UNION ALL ", subQueries);
        String sql = """
                SELECT
                  CASE WHEN COUNT(*) = 1 THEN MIN(id)           ELSE NULL  END AS id,
                  CASE WHEN COUNT(*) = 1 THEN MIN(object_type)  ELSE NULL  END AS object_type,
                  CASE WHEN COUNT(*) = 1 THEN MIN(title)         ELSE NULL  END AS title,
                  AVG(latitude)                                               AS latitude,
                  AVG(longitude)                                              AS longitude,
                  CASE WHEN COUNT(*) = 1 THEN MIN(cover_image_id) ELSE NULL END AS cover_image_id,
                  CASE WHEN COUNT(*) = 1 THEN MIN(cover_image_url) ELSE NULL END AS cover_image_url,
                  COUNT(*)                                                    AS count
                FROM (
                """ + union + """
                )
                GROUP BY CAST(ROUND(latitude  / ?) AS INT),
                         CAST(ROUND(longitude / ?) AS INT)
                """;

        params.add(gridSize);
        params.add(gridSize);

        try (var conn = SQLiteConnectionFactory.getSpatialConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<MapDataPoint> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            log.error("Failed to query map markers for userId='{}'", userId, e);
            throw new RuntimeException("Database error while querying map markers", e);
        }
    }

    @Override
    public synchronized Page<MapDataPoint> getClusterItems(
            UUID userId,
            double clusterLatitude, double clusterLongitude,
            double gridSize,
            Set<MapObjectType> filter,
            PageRequest pageRequest) {

        // Identify the grid cell from the cluster centroid
        int gridRow = (int) Math.round(clusterLatitude  / gridSize);
        int gridCol = (int) Math.round(clusterLongitude / gridSize);

        // Cell bounds with small buffer to account for floating-point rounding
        double halfCell = gridSize / 2.0 + gridSize * 0.01;
        double cellMinLat = gridRow * gridSize - halfCell;
        double cellMaxLat = gridRow * gridSize + halfCell;
        double cellMinLon = gridCol * gridSize - halfCell;
        double cellMaxLon = gridCol * gridSize + halfCell;

        boolean all = filter.isEmpty() || filter.contains(MapObjectType.ALL);
        boolean places  = all || filter.contains(MapObjectType.PLACE);
        boolean routes  = all || filter.contains(MapObjectType.ROUTE);
        boolean stories = all || filter.contains(MapObjectType.STORY);

        List<String> subQueries = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (places) {
            subQueries.add(PLACES_SUB);
            // For places, use BuildMbr spatial filter
            params.add(userId.toString());
            params.add(cellMinLon);
            params.add(cellMinLat);
            params.add(cellMaxLon);
            params.add(cellMaxLat);
        }
        if (routes) {
            subQueries.add(ROUTES_SUB);
            params.add(userId.toString());
            params.add(cellMinLat);
            params.add(cellMaxLat);
            params.add(cellMinLon);
            params.add(cellMaxLon);
        }
        if (stories) {
            subQueries.add(STORIES_SUB);
            params.add(userId.toString());
            params.add(cellMinLat);
            params.add(cellMaxLat);
            params.add(cellMinLon);
            params.add(cellMaxLon);
        }

        if (subQueries.isEmpty()) return Page.empty(pageRequest);

        String union = String.join(" UNION ALL ", subQueries);
        // No GROUP BY — return individual rows, paginated
        String sql = "SELECT id, object_type, title, latitude, longitude, cover_image_id, cover_image_url, 1 AS count FROM (" + union + ") ORDER BY id LIMIT ? OFFSET ?";

        params.add(pageRequest.size() + 1);
        params.add(pageRequest.offset());

        try (var conn = SQLiteConnectionFactory.getSpatialConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<MapDataPoint> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
                boolean hasNext = items.size() > pageRequest.size();
                if (hasNext) items.remove(items.size() - 1);
                return Page.of(items, pageRequest, hasNext);
            }
        } catch (SQLException e) {
            log.error("Failed to query cluster items for userId='{}'", userId, e);
            throw new RuntimeException("Database error while querying cluster items", e);
        }
    }

    private MapDataPoint mapRow(ResultSet rs) throws SQLException {
        String idStr    = rs.getString("id");
        String typeStr  = rs.getString("object_type");
        String title    = rs.getString("title");
        double latitude = rs.getDouble("latitude");
        double longitude = rs.getDouble("longitude");
        String imgIdStr = rs.getString("cover_image_id");
        String imgUrl   = rs.getString("cover_image_url");
        int count       = rs.getInt("count");

        UUID id = idStr != null ? UUID.fromString(idStr) : null;

        MapObjectType objectType = null;
        if (typeStr != null) {
            objectType = MapObjectType.valueOf(typeStr);
        }

        UUID coverImageId = imgIdStr != null ? UUID.fromString(imgIdStr) : null;
        Path coverImageUrl = imgUrl != null ? Path.of(imgUrl) : null;

        return new MapDataPoint(id, objectType, title, latitude, longitude, coverImageId, coverImageUrl, count);
    }
}
