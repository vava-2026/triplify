package com.triplify.infrastructure.unsplash;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.triplify.domain.service.ImageStorageService;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Singleton
public class UnsplashImageSeeder {

    private static final Logger log = LoggerFactory.getLogger(UnsplashImageSeeder.class);

    private static final String DEFAULT_IMAGE_ID      = "40000000-0000-0000-0000-000000000001";
    private static final String PLACES_RESOURCE_PATH  = "seeders/place_images/";
    private static final String ROUTES_RESOURCE_PATH  = "seeders/place_images/routes/";
    private static final String STORIES_RESOURCE_PATH = "seeders/place_images/stories/";

    private final ImageStorageService imageStorage;

    @Inject
    public UnsplashImageSeeder(ImageStorageService imageStorage) {
        this.imageStorage = imageStorage;
    }

    public void seed() {
        seedPlaces();
        seedRoutes();
        seedStories();
        seedTrips();
    }

    // -------------------------------------------------------------------------
    // Places
    // -------------------------------------------------------------------------

    private void seedPlaces() {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            List<Row> places = loadUnseededPlaces(conn);

            if (places.isEmpty()) {
                log.info("UnsplashImageSeeder: all places already have custom images, skipping.");
                return;
            }

            log.info("UnsplashImageSeeder: seeding {} place(s)...", places.size());
            conn.setAutoCommit(false);
            int success = 0, failed = 0;

            try {
                for (Row place : places) {
                    if (seedPlace(conn, place)) success++;
                    else                        failed++;
                }
                conn.commit();
                log.info("UnsplashImageSeeder places done: {} seeded, {} failed.", success, failed);
            } catch (Exception e) {
                conn.rollback();
                log.error("Transaction failed, rolling back place seeding changes", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            log.error("Failed to establish database connection for place seeding", e);
        }
    }

    private boolean seedPlace(Connection conn, Row place) {
        String resourceName = PLACES_RESOURCE_PATH + place.title() + ".jpg";

        try (InputStream in = getClass().getResourceAsStream("/" + resourceName)) {
            if (in == null) {
                log.warn("Bundled image not found for place '{}': {}", place.title(), resourceName);
                return false;
            }

            Path tempFile = Files.createTempFile("triplify-place-", ".jpg");
            try {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                Path storedPath = imageStorage.store(tempFile);
                attachPlaceImage(conn, place.id(), storedPath.toString(), place.title());
                log.debug("Seeded place '{}' -> '{}'", place.title(), storedPath.getFileName());
                return true;
            } finally {
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            log.error("Error seeding place '{}': {}", place.title(), e.getMessage());
            return false;
        }
    }

    private List<Row> loadUnseededPlaces(Connection conn) throws SQLException {
        String sql = "SELECT id, title FROM places WHERE cover_image_id = ? ORDER BY rowid";
        List<Row> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DEFAULT_IMAGE_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Row(rs.getString("id"), rs.getString("title")));
                }
            }
        }
        return result;
    }

    private void attachPlaceImage(Connection conn, String placeId, String localPath, String description) throws SQLException {
        String imageId = UUID.randomUUID().toString();
        String now     = Instant.now().toString();

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO images (id, url, description, uploaded_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, imageId);
            ps.setString(2, localPath);
            ps.setString(3, description);
            ps.setString(4, now);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE places SET cover_image_id = ?, updated_at = ? WHERE id = ?")) {
            ps.setString(1, imageId);
            ps.setString(2, now);
            ps.setString(3, placeId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Routes
    // -------------------------------------------------------------------------

    private void seedRoutes() {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            List<Row> routes = loadUnseededRoutes(conn);

            if (routes.isEmpty()) {
                log.info("UnsplashImageSeeder: all routes already have cover images, skipping.");
                return;
            }

            log.info("UnsplashImageSeeder: seeding {} route(s)...", routes.size());
            conn.setAutoCommit(false);
            int success = 0, failed = 0;

            try {
                for (Row route : routes) {
                    if (seedRoute(conn, route)) success++;
                    else                        failed++;
                }
                conn.commit();
                log.info("UnsplashImageSeeder routes done: {} seeded, {} failed.", success, failed);
            } catch (Exception e) {
                conn.rollback();
                log.error("Transaction failed, rolling back route seeding changes", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            log.error("Failed to establish database connection for route seeding", e);
        }
    }

    private boolean seedRoute(Connection conn, Row route) {
        String resourceName = ROUTES_RESOURCE_PATH + route.title() + ".jpg";

        try (InputStream in = getClass().getResourceAsStream("/" + resourceName)) {
            if (in == null) {
                log.warn("Bundled image not found for route '{}': {}", route.title(), resourceName);
                return false;
            }

            Path tempFile = Files.createTempFile("triplify-route-", ".jpg");
            try {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                Path storedPath = imageStorage.store(tempFile);
                attachRouteImage(conn, route.id(), storedPath.toString(), route.title());
                log.debug("Seeded route '{}' -> '{}'", route.title(), storedPath.getFileName());
                return true;
            } finally {
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            log.error("Error seeding route '{}': {}", route.title(), e.getMessage());
            return false;
        }
    }

    private List<Row> loadUnseededRoutes(Connection conn) throws SQLException {
        String sql = "SELECT id, title FROM routes WHERE cover_image_id IS NULL ORDER BY rowid";
        List<Row> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Row(rs.getString("id"), rs.getString("title")));
                }
            }
        }
        return result;
    }

    private void attachRouteImage(Connection conn, String routeId, String localPath, String description) throws SQLException {
        String imageId = UUID.randomUUID().toString();
        String now     = Instant.now().toString();

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO images (id, url, description, uploaded_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, imageId);
            ps.setString(2, localPath);
            ps.setString(3, description);
            ps.setString(4, now);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE routes SET cover_image_id = ?, updated_at = ? WHERE id = ?")) {
            ps.setString(1, imageId);
            ps.setString(2, now);
            ps.setString(3, routeId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Stories
    // -------------------------------------------------------------------------

    private void seedStories() {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            List<Row> stories = loadUnseededStories(conn);

            if (stories.isEmpty()) {
                log.info("UnsplashImageSeeder: all stories already have images, skipping.");
                return;
            }

            log.info("UnsplashImageSeeder: seeding {} story images...", stories.size());
            conn.setAutoCommit(false);
            int success = 0, failed = 0;

            try {
                for (Row story : stories) {
                    if (seedStory(conn, story)) success++;
                    else                        failed++;
                }
                conn.commit();
                log.info("UnsplashImageSeeder stories done: {} seeded, {} failed.", success, failed);
            } catch (Exception e) {
                conn.rollback();
                log.error("Transaction failed, rolling back story seeding changes", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            log.error("Failed to establish database connection for story seeding", e);
        }
    }

    private boolean seedStory(Connection conn, Row story) {
        String resourceName = STORIES_RESOURCE_PATH + story.title() + ".jpg";

        try (InputStream in = getClass().getResourceAsStream("/" + resourceName)) {
            if (in == null) {
                log.warn("Bundled image not found for story '{}': {}", story.title(), resourceName);
                return false;
            }

            Path tempFile = Files.createTempFile("triplify-story-", ".jpg");
            try {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                Path storedPath = imageStorage.store(tempFile);
                attachStoryImage(conn, story.id(), storedPath.toString(), story.title());
                log.debug("Seeded story '{}' -> '{}'", story.title(), storedPath.getFileName());
                return true;
            } finally {
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            log.error("Error seeding story '{}': {}", story.title(), e.getMessage());
            return false;
        }
    }

    private List<Row> loadUnseededStories(Connection conn) throws SQLException {
        String sql = """
                SELECT s.id, s.title
                FROM stories s
                LEFT JOIN story_images si ON si.story_id = s.id
                WHERE si.story_id IS NULL
                ORDER BY s.rowid
                """;
        List<Row> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Row(rs.getString("id"), rs.getString("title")));
            }
        }
        return result;
    }

    private void attachStoryImage(Connection conn, String storyId, String localPath, String description) throws SQLException {
        String imageId = UUID.randomUUID().toString();
        String now     = Instant.now().toString();

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO images (id, url, description, uploaded_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, imageId);
            ps.setString(2, localPath);
            ps.setString(3, description);
            ps.setString(4, now);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO story_images (story_id, image_id) VALUES (?, ?)")) {
            ps.setString(1, storyId);
            ps.setString(2, imageId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Trips
    // -------------------------------------------------------------------------

    private void seedTrips() {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            List<TripRow> trips = loadUnseededTrips(conn);

            if (trips.isEmpty()) {
                log.info("UnsplashImageSeeder: all trips already have cover images, skipping.");
                return;
            }

            log.info("UnsplashImageSeeder: seeding {} trip cover image(s)...", trips.size());
            conn.setAutoCommit(false);
            int success = 0, failed = 0;

            try {
                for (TripRow trip : trips) {
                    attachTripCoverImage(conn, trip.id(), trip.imageId());
                    log.debug("Seeded trip '{}' with image from story", trip.title());
                    success++;
                }
                conn.commit();
                log.info("UnsplashImageSeeder trips done: {} seeded, {} failed.", success, failed);
            } catch (Exception e) {
                conn.rollback();
                log.error("Transaction failed, rolling back trip seeding changes", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            log.error("Failed to establish database connection for trip seeding", e);
        }
    }

    private List<TripRow> loadUnseededTrips(Connection conn) throws SQLException {
        String sql = """
                SELECT t.id, t.title, MIN(si.image_id) AS image_id
                FROM trips t
                JOIN stories s ON s.trip_id = t.id
                JOIN story_images si ON si.story_id = s.id
                WHERE t.cover_image_id IS NULL
                GROUP BY t.id
                ORDER BY t.rowid
                """;
        List<TripRow> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new TripRow(rs.getString("id"), rs.getString("title"), rs.getString("image_id")));
            }
        }
        return result;
    }

    private void attachTripCoverImage(Connection conn, String tripId, String imageId) throws SQLException {
        String now = Instant.now().toString();

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE trips SET cover_image_id = ?, updated_at = ? WHERE id = ?")) {
            ps.setString(1, imageId);
            ps.setString(2, now);
            ps.setString(3, tripId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------

    private record Row(String id, String title) {}

    private record TripRow(String id, String title, String imageId) {}
}
