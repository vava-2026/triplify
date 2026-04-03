package com.triplify.infrastructure.repository;

import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.model.Country;
import com.triplify.domain.model.Place;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.PlaceRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlaceRepositoryImpl implements PlaceRepository {

    private static final Logger log = LoggerFactory.getLogger(CountryRepositoryImpl.class);

    @Override
    public Optional<Place> findById(String id) {
        String sql = "SELECT id, user_id, country_id, cover_image_id, title, description, latitude, longitude, created_at, updated_at " +
        "FROM places WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        catch (SQLException e) {
                log.error("Error fetching Place by id='{}'", id, e);
                throw new RuntimeException("Database error while finding place by id", e);
        }
        return Optional.empty();
    }

    public Page<Place> findAll(PageRequest page, PlaceFilter filter) {
        String sql = "SELECT id, user_id, country_id, cover_image_id, title, description, latitude, longitude, created_at, updated_at " +
                "FROM places " +
                "WHERE 1=1 ";

        if (filter.countryId() != null) {
            sql += "AND country_id = ? ";
        }
        if (filter.name() != null) {
            sql += "AND title LIKE ? ";
        }
        sql += "LIMIT ? OFFSET ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            int currentIndex = 1;
            if (filter.countryId() != null) {
                ps.setString(currentIndex++, filter.countryId());
            }
            if (filter.name() != null) {
                // TODO: do we want it here, or should the user specify '%' themselves?
                ps.setString(currentIndex++, filter.name() + "%");
            }

            ps.setInt(currentIndex++, page.size());
            ps.setInt(currentIndex++, page.offset());
            try (ResultSet rs = ps.executeQuery()) {
                List<Place> places = new ArrayList<>();
                while (rs.next()) {
                    places.add(mapRow(rs));
                }
                return Page.of(places, page, places.size());
            }
        }
        catch (SQLException e) {
            log.error("Error finding Place with name='{}'", filter.name(), e);
            throw new RuntimeException("Database error while finding place", e);
        }
    }

    public void create(Place place) {
        String sql = "INSERT INTO places (id, user_id, country_id, cover_image_id, title, description, latitude, longitude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, place.getId().toString());
            ps.setString(2, place.getUserId().toString());
            ps.setString(3, place.getCountryId().toString());
            setNullableUUID(ps, 4, place.getCoverImageId());
            ps.setString(5, place.getTitle());
            ps.setString(6, place.getDescription());
            ps.setDouble(7, place.getLatitude());
            ps.setDouble(8, place.getLongitude());

            ps.executeUpdate();

            log.debug("Created Place with id='{}', title='{}'", place.getId(), place.getTitle());
        }
        catch(SQLException e) {
            log.error("Error creating Place with id='{}'", place.getId(), e);
            throw new RuntimeException("Database error while creating place", e);
        }
    }

    public void delete(Place place) {
        String sql = "DELETE FROM places WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, place.getId().toString());
            ps.executeUpdate();

            log.debug("Deleted Place with id='{}', name='{}'", place.getId(), place.getTitle());
        }
        catch (SQLException e) {
            log.error("Error deleting Place with id='{}'", place.getId(), e);
            throw new RuntimeException("Database error while deleting place", e);
        }
    }

    public void update(Place place) {
        String sql = "UPDATE places " +
                "SET user_id = ?, country_id = ?, cover_image_id = ?, title = ?, description = ?, latitude = ?, longitude = ? " +
                "WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, place.getUserId().toString());
            ps.setString(2, place.getCountryId().toString());
            setNullableUUID(ps, 3, place.getCoverImageId());
            ps.setString(4, place.getTitle());
            ps.setString(5, place.getDescription());
            ps.setDouble(6, place.getLatitude());
            ps.setDouble(7, place.getLongitude());

            ps.setString(8, place.getId().toString());
            ps.executeUpdate();

            log.debug("Updated Place with id='{}', title='{}'", place.getId(), place.getTitle());
        }
        catch (SQLException e) {
            log.error("Error updating Place with id='{}'", place.getId(), e);
            throw new RuntimeException("Database error while updating place", e);
        }
    }

    private Place mapRow(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID userId = UUID.fromString(rs.getString("user_id"));
        UUID countryId = UUID.fromString(rs.getString("country_id"));
        String coverImageIdStr = rs.getString("cover_image_id");
        UUID coverImageId = coverImageIdStr != null ? UUID.fromString(coverImageIdStr) : null;
        String title = rs.getString("title");
        String description = rs.getString("description");
        double latitude = rs.getDouble("latitude");
        double longitude = rs.getDouble("longitude");
        Instant created_at = Instant.parse(rs.getString("created_at"));
        Instant updated_at = Instant.parse(rs.getString("updated_at"));

        return new Place(id, userId, countryId, coverImageId, title, description, latitude, longitude, created_at, updated_at);
    }

    private void setNullableUUID(PreparedStatement ps, int index, UUID value) throws SQLException {
        if (value != null) {
            setNullableString(ps, index, value.toString());
        }
        else {
            setNullableString(ps, index, null);
        }
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null) {
            ps.setString(index, value);
        }
        else {
            ps.setNull(index, java.sql.Types.VARCHAR);
        }
    }
}
