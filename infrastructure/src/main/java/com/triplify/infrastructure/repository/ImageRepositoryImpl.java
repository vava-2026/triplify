package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Image;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.ImageRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ImageRepositoryImpl implements ImageRepository {

    private static final Logger log = LoggerFactory.getLogger(ImageRepositoryImpl.class);

    @Override
    public void save(Image image) {
        String sql = "INSERT INTO images (id, url, description, uploaded_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, image.getId().toString());
            ps.setString(2, image.getUrl().toString());
            ps.setString(3, image.getDescription());
            ps.setString(4, image.getUploadedAt().toString());
            ps.executeUpdate();
            log.debug("Image saved: id={}", image.getId());
        } catch (SQLException e) {
            log.error("Failed to save image id='{}'", image.getId(), e);
            throw new RuntimeException("Database error while saving image", e);
        }
    }

    @Override
    public Optional<Image> findById(UUID id) {
        String sql = "SELECT id, url, description, uploaded_at FROM images WHERE id = ? LIMIT 1";
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find image by id='{}'", id, e);
            throw new RuntimeException("Database error while finding image by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Page<Image> findAll(PageRequest pageRequest, Instant uploadedFrom, Instant uploadedTo, boolean uploadTimeAsc) {
        String order = uploadTimeAsc ? "ASC" : "DESC";

        StringBuilder where = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (uploadedFrom != null) {
            where.append(where.isEmpty() ? " WHERE " : " AND ").append("uploaded_at >= ?");
            args.add(uploadedFrom.toString());
        }
        if (uploadedTo != null) {
            where.append(where.isEmpty() ? " WHERE " : " AND ").append("uploaded_at <= ?");
            args.add(uploadedTo.toString());
        }

        String countSql = "SELECT COUNT(*) FROM images" + where;
        String dataSql  = "SELECT id, url, description, uploaded_at FROM images"
                + where
                + " ORDER BY uploaded_at " + order
                + " LIMIT ? OFFSET ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            long total;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                bindArgs(ps, args, 1);
                try (ResultSet rs = ps.executeQuery()) {
                    total = rs.next() ? rs.getLong(1) : 0;
                }
            }

            List<Image> items = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                int idx = bindArgs(ps, args, 1);
                ps.setInt(idx++, pageRequest.size());
                ps.setInt(idx, pageRequest.offset());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(mapRow(rs));
                }
            }

            return Page.of(items, pageRequest, total);
        } catch (SQLException e) {
            log.error("Failed to query images", e);
            throw new RuntimeException("Database error while querying images", e);
        }
    }

    @Override
    public void update(Image image) {
        String sql = "UPDATE images SET url = ?, description = ? WHERE id = ?";
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, image.getUrl().toString());
            ps.setString(2, image.getDescription());
            ps.setString(3, image.getId().toString());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                log.warn("Update affected 0 rows for image id='{}'", image.getId());
            } else {
                log.debug("Image updated: id={}", image.getId());
            }
        } catch (SQLException e) {
            log.error("Failed to update image id='{}'", image.getId(), e);
            throw new RuntimeException("Database error while updating image", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM images WHERE id = ?";
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            int rows = ps.executeUpdate();
            log.debug("Image deleted: id={}, rows affected={}", id, rows);
        } catch (SQLException e) {
            log.error("Failed to delete image id='{}'", id, e);
            throw new RuntimeException("Database error while deleting image", e);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Image mapRow(ResultSet rs) throws SQLException {
        UUID id          = UUID.fromString(rs.getString("id"));
        Path url         = Path.of(rs.getString("url"));
        String desc      = rs.getString("description");
        Instant uploaded = Instant.parse(rs.getString("uploaded_at"));
        return new Image(id, url, desc, uploaded);
    }

    /** Binds each string argument in order starting from {@code startIndex}.
     *  Returns the next free parameter index. */
    private int bindArgs(PreparedStatement ps, List<String> args, int startIndex) throws SQLException {
        int idx = startIndex;
        for (String arg : args) {
            ps.setString(idx++, arg);
        }
        return idx;
    }
}
