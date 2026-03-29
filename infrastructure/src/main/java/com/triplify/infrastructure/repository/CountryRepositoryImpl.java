package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Country;
import com.triplify.domain.repository.CountryRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Optional;

public class CountryRepositoryImpl implements CountryRepository {

    private static final Logger log = LoggerFactory.getLogger(CountryRepositoryImpl.class);

    @Override
    public java.util.Optional<com.triplify.domain.model.Country> findById(String id) {
        String sql = "SELECT id, created_by_id, name, name_sk, emoji_unicode, is_available " +
                "FROM countries WHERE id = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        catch(SQLException e) {
            log.error("Failed to find country by id='{}'", id, e);
            throw new RuntimeException("Database error while finding country by name", e);
        }
        return Optional.empty();
    }

    public boolean existsByName(String name, String nameSk) {
        String sql = "SELECT COUNT(*) FROM countries WHERE name = ? OR name_sk = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, nameSk);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
        catch(SQLException e) {
            log.error("Failed to find country by name='{}' or nameSk='{}'", name, nameSk, e);
            throw new RuntimeException("Database error while finding country by name", e);
        }
    }

    @Override
    public void delete(Country country) {
        String sql = "DELETE FROM countries WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country.getId().toString());
            ps.executeUpdate();

            log.debug("Deleted country with id='{}', name='{}'", country.getId(), country.getName());
        }
        catch (SQLException e) {
            log.error("Failed to delete country with id='{}'", country.getId(), e);
            throw new RuntimeException("Database error while deleting country", e);
        }
    }

    @Override
    public void create(Country country) {
        String sql = "INSERT INTO countries (id, created_by_id, name, name_sk, emoji_unicode, is_available)" +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, country.getId().toString());
            ps.setString(2, country.getCreatedById().toString());
            ps.setString(3, country.getName());
            ps.setString(4, country.getNameSk());
            ps.setString(5, country.getEmojiUnicode());
            ps.setBoolean(6, country.isAvailable());
            ps.executeUpdate();

            log.debug("Created country with id='{}', name='{}'", country.getId(), country.getName());
        }
        catch (SQLException e) {
            log.error("Failed to create country with id='{}'", country.getId(), e);
            throw new RuntimeException("Database error while creating country", e);
        }
    }

    @Override
    public void update(Country country) {
        String sql = "UPDATE countries" +
                "SET created_by_id = ?, name = ?, name_sk = ?, emoji_unicode = ?, is_available = ?" +
                "WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, country.getCreatedById().toString());
            ps.setString(2, country.getName());
            ps.setString(3, country.getNameSk());
            ps.setString(4, country.getEmojiUnicode());
            ps.setBoolean(5, country.isAvailable());

            ps.setString(6, country.getId().toString());
            ps.executeUpdate();

            log.debug("Updated country with id='{}', name='{}'", country.getId(), country.getName());
        }
        catch (SQLException e) {
            log.error("Failed to Update country with id='{}'", country.getId(), e);
            throw new RuntimeException("Database error while updating country", e);
        }
    }

    private Country mapRow(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID createdById = UUID.fromString(rs.getString("created_by_id"));
        String name = rs.getString("name");
        String nameSk = rs.getString("name_sk");
        String emojiUnicode = rs.getString("emoji_unicode");
        boolean isAvailable = rs.getBoolean("is_available");

        return new Country(id, createdById, name, nameSk, emojiUnicode, isAvailable);
    }
}
