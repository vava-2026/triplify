package com.triplify.infrastructure.unsplash;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.triplify.domain.service.ImageStorageService;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private static final String DEFAULT_IMAGE_ID   = "40000000-0000-0000-0000-000000000001";
    private static final String RESOURCE_BASE_PATH = "seeders/place_images/";

    private final ImageStorageService imageStorage;

    @Inject
    public UnsplashImageSeeder(ImageStorageService imageStorage) {
        this.imageStorage = imageStorage;
    }

    public void seed() {
        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            List<PlaceRow> places = loadUnseededPlaces(conn);

            if (places.isEmpty()) {
                log.info("UnsplashImageSeeder: all places already have custom images, skipping.");
                return;
            }

            log.info("UnsplashImageSeeder: seeding {} place(s) from bundled resources...", places.size());

            conn.setAutoCommit(false);
            int success = 0, failed = 0;

            try {
                for (PlaceRow place : places) {
                    if (seedPlace(conn, place)) success++;
                    else                  failed++;
                }
                conn.commit();
                log.info("UnsplashImageSeeder done: {} seeded, {} failed.", success, failed);
            } catch (Exception e) {
                conn.rollback();
                log.error("Transaction failed, rolling back seeding changes", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            log.error("Failed to establish database connection for seeding", e);
        }
    }

    private boolean seedPlace(Connection conn, PlaceRow place) {
        String resourceName = RESOURCE_BASE_PATH + place.title() + ".jpg";

        try (InputStream in = getClass().getResourceAsStream("/" + resourceName)) {
            if (in == null) {
                log.warn("Bundled image not found for place '{}': {}", place.title(), resourceName);
                return false;
            }

            Path tempFile = Files.createTempFile("triplify-place-", ".jpg");
            try {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                Path storedPath = imageStorage.store(tempFile);
                attachImage(conn, place.id(), storedPath.toString(), place.title());
                log.debug("Seeded '{}' -> '{}'", place.title(), storedPath.getFileName());
                return true;
            } finally {
                Files.deleteIfExists(tempFile);
            }

        } catch (IOException | SQLException e) {
            log.error("Error seeding place '{}': {}", place.title(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Failed to seed place '{}': {}", place.title(), e.getMessage());
            return false;
        }
    }

    private List<PlaceRow> loadUnseededPlaces(Connection conn) throws SQLException {
        String sql = "SELECT id, title FROM places WHERE cover_image_id = ? ORDER BY rowid";
        List<PlaceRow> result = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DEFAULT_IMAGE_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new PlaceRow(rs.getString("id"), rs.getString("title")));
                }
            }
        }
        return result;
    }

    private void attachImage(Connection conn, String placeId, String localPath, String description) throws SQLException {
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

    private record PlaceRow(String id, String title) {}
}