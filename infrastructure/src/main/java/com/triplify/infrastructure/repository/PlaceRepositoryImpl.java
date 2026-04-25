package com.triplify.infrastructure.repository;

import com.triplify.domain.filter.PlaceFilter;
import com.triplify.domain.model.Country;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.Place;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.PlaceRepository;
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

public class PlaceRepositoryImpl implements PlaceRepository {

    private static final Logger log = LoggerFactory.getLogger(PlaceRepositoryImpl.class);

    private static final String PLACE_WITH_COUNTRY_AND_IMAGE_SELECT = """
        SELECT 
            p.id AS p_id, p.user_id AS p_user_id, p.country_id AS p_country_id, 
            p.cover_image_id AS p_cover_image_id, p.title AS p_title, 
            p.description AS p_description, p.latitude AS p_latitude, 
            p.longitude AS p_longitude, p.created_at AS p_created_at, p.updated_at AS p_updated_at,
                
            c.id AS c_id, c.created_by AS c_created_by, c.name AS c_name, 
            c.name_sk AS c_name_sk, c.emoji_unicode AS c_emoji_unicode, c.is_available AS c_is_available,
                
            i.id AS i_id, i.url AS i_url, i.description AS i_description, i.uploaded_at AS i_uploaded_at
        FROM places p
        INNER JOIN countries c ON p.country_id = c.id
        LEFT JOIN images i ON p.cover_image_id = i.id """;

    @Override
    public Optional<Place> findById(UUID id) {
        String sql = PLACE_WITH_COUNTRY_AND_IMAGE_SELECT + " WHERE p.id = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPlaceWithRelations(rs));
                }
            }
        }
        catch (SQLException e) {
                log.error("Error fetching Place by id='{}'", id, e);
                throw new RuntimeException("Database error while finding place by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Page<Place> findList(PageRequest page, PlaceFilter filter, UUID userId) {
        String sql = PLACE_WITH_COUNTRY_AND_IMAGE_SELECT + " WHERE p.user_id = ? ";

        List<Object> params = new ArrayList<>();
        params.add(userId.toString());

        if (filter.countryId() != null && !filter.countryId().isBlank()) {
            sql += "AND p.country_id = ? ";
            params.add(filter.countryId());
        }
        if (filter.name() != null && !filter.name().isBlank()) {
            sql += "AND p.title LIKE ? ";
            params.add("%"+filter.name() + "%");
        }

        sql += "LIMIT ? OFFSET ?";
        params.add(page.size() + 1);
        params.add(page.offset());

        try (Connection conn = SQLiteConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int j = 0; j < params.size(); j++) {
                ps.setObject(j + 1, params.get(j));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Place> places = new ArrayList<>();
                while (rs.next()) {
                    places.add(mapPlaceWithRelations(rs));
                }
                boolean hasNext = places.size() > page.size();
                if (hasNext) {
                    places.remove(places.size() - 1);
                }
                return Page.of(places, page, hasNext);
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

    private Place mapPlaceWithRelations(ResultSet rs) throws SQLException {
        Country country = new Country(
                UUID.fromString(rs.getString("c_id")),
                rs.getString("c_created_by") != null? UUID.fromString(rs.getString("c_created_by")):null,
                rs.getString("c_name"),
                rs.getString("c_name_sk"),
                rs.getString("c_emoji_unicode"),
                rs.getBoolean("c_is_available")
        );

        Image coverImage = null;
        String imageIdStr = rs.getString("i_id");
        if (imageIdStr != null) {
            coverImage = new Image(
                    UUID.fromString(imageIdStr),
                    Path.of(rs.getString("i_url")),
                    rs.getString("i_description"),
                    Instant.parse(rs.getString("i_uploaded_at"))
            );
        }

        return new Place(
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
