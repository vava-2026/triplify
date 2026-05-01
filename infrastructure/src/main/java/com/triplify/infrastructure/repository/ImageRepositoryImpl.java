package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Image;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.ImageRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.triplify.domain.model.enums.ImageOwnerType;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
                if (rs.next()) return Optional.of(toDomainImage(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find image by id='{}'", id, e);
            throw new RuntimeException("Database error while finding image by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Page<Image> findAll(
            UUID userId,
            PageRequest pageRequest,
            String ownerId,
            ImageOwnerType ownerType,
            Instant uploadedFrom,
            Instant uploadedTo,
            boolean uploadTimeAsc) {
        StringBuilder where = new StringBuilder();
        List<String> args = new ArrayList<>();

        String order = uploadTimeAsc ? "ASC" : "DESC";

        if (ownerType != null) {
            boolean isOwnerId = ownerId != null && !ownerId.isBlank();
            String ownerExistsClause = toOwnerExistsClause(ownerType, isOwnerId);
            where.append(where.isEmpty() ? " WHERE " : " AND ").append(ownerExistsClause);
            args.add(userId.toString());
            if (isOwnerId) {
                args.add(ownerId);
            }
        }

        if (uploadedFrom != null) {
            where.append(where.isEmpty() ? " WHERE " : " AND ").append("uploaded_at >= ?");
            args.add(uploadedFrom.toString());
        }
        if (uploadedTo != null) {
            where.append(where.isEmpty() ? " WHERE " : " AND ").append("uploaded_at <= ?");
            args.add(uploadedTo.toString());
        }

        String dataSql  = "SELECT id, url, description, uploaded_at FROM images"
                + where
                + " ORDER BY uploaded_at " + order
                + " LIMIT ? OFFSET ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection()) {
            List<Image> items = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                int idx = bindArgs(ps, args, 1);
                int fetchLimit = pageRequest.size() + 1;
                ps.setInt(idx++, fetchLimit);
                ps.setInt(idx, pageRequest.offset());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(toDomainImage(rs));
                }
            }

            boolean hasNext = items.size() > pageRequest.size();
            if (hasNext) {
                items.remove(items.size() - 1);
            }
            return Page.of(items, pageRequest, hasNext);
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
            if (rows == 0) {
                log.warn("Delete affected 0 rows for image id='{}'", id);
            }else {
                log.debug("Image deleted: id={}", id);
            }
        } catch (SQLException e) {
            log.error("Failed to delete image id='{}'", id, e);
            throw new RuntimeException("Database error while deleting image", e);
        }
    }

    private Image toDomainImage(ResultSet rs) throws SQLException {
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

    @Override
    public void linkToOwner(UUID imageId, UUID ownerId, ImageOwnerType ownerType) {
        String table = resolveJunctionTable(ownerType);
        String ownerCol = resolveOwnerColumn(ownerType);
        if (table == null || ownerCol == null) {
            log.warn("Cannot link image: unsupported ownerType='{}'", ownerType);
            return;
        }
        String sql = "INSERT OR IGNORE INTO " + table + " (" + ownerCol + ", image_id) VALUES (?, ?)";
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ownerId.toString());
            ps.setString(2, imageId.toString());
            ps.executeUpdate();
            log.debug("Image linked: imageId={} ownerId={} ownerType={}", imageId, ownerId, ownerType);
        } catch (SQLException e) {
            log.error("Failed to link image id='{}' to owner='{}' type='{}'", imageId, ownerId, ownerType, e);
            throw new RuntimeException("Database error while linking image to owner", e);
        }
    }

    @Override
    public void unlinkFromOwner(UUID imageId, UUID ownerId, ImageOwnerType ownerType) {
        String table = resolveJunctionTable(ownerType);
        String ownerCol = resolveOwnerColumn(ownerType);
        if (table == null || ownerCol == null) {
            log.warn("Cannot unlink image: unsupported ownerType='{}'", ownerType);
            return;
        }
        String sql = "DELETE FROM " + table + " WHERE " + ownerCol + " = ? AND image_id = ?";
        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ownerId.toString());
            ps.setString(2, imageId.toString());
            ps.executeUpdate();
            log.debug("Image unlinked: imageId={} ownerId={} ownerType={}", imageId, ownerId, ownerType);
        } catch (SQLException e) {
            log.error("Failed to unlink image id='{}' from owner='{}' type='{}'", imageId, ownerId, ownerType, e);
            throw new RuntimeException("Database error while unlinking image from owner", e);
        }
    }

    private String resolveJunctionTable(ImageOwnerType ownerType) {
        if (ownerType == null) return null;
        return switch (ownerType) {
            case TRIP -> "trip_images";
            case TRIP_ROUTE -> "trip_route_images";
            case TRIP_PLACE -> "trip_places_images";
            case STORY -> "story_images";
        };
    }

    private String resolveOwnerColumn(ImageOwnerType ownerType) {
        if (ownerType == null) return null;
        return switch (ownerType) {
            case TRIP-> "trip_id";
            case TRIP_ROUTE -> "trip_route_id";
            case TRIP_PLACE -> "trip_place_id";
            case STORY -> "story_id";
        };
    }

    private String toOwnerExistsClause(ImageOwnerType ownerType, boolean isOwnerId) {
        var value = switch (ownerType) {
            case TRIP -> "EXISTS (SELECT 1 FROM trip_images ti JOIN trips t ON t.id = ti.trip_id WHERE ti.image_id = images.id AND t.user_id = ?";
            case TRIP_ROUTE -> "EXISTS (SELECT 1 FROM trip_route_images tri JOIN trip_routes tr ON tr.id = tri.trip_route_id JOIN trips t ON t.id = tr.trip_id WHERE tri.image_id = images.id AND t.user_id = ?";
            case TRIP_PLACE -> "EXISTS (SELECT 1 FROM trip_places_images tpi JOIN trip_places tp ON tp.id = tpi.trip_place_id JOIN trips t ON t.id = tp.trip_id WHERE tpi.image_id = images.id AND t.user_id = ?";
            case STORY -> "EXISTS (SELECT 1 FROM story_images si JOIN stories s ON s.id = si.story_id WHERE si.image_id = images.id AND s.user_id = ?";
        };

        if (isOwnerId) {
            var idPart = switch (ownerType) {
                case TRIP -> " AND ti.trip_id = ?";
                case TRIP_ROUTE -> " AND tri.trip_route_id = ?";
                case TRIP_PLACE -> " AND tpi.trip_place_id = ?";
                case STORY -> " AND si.story_id = ?";
            };
            value += idPart;
        }
        value += ")";
        return value;
    }
}
