package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Statistic;
import com.triplify.domain.model.enums.StatisticType;
import com.triplify.domain.repository.StatisticRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StatisticRepositoryImpl implements StatisticRepository {

    private static final Logger log = LoggerFactory.getLogger(StatisticRepositoryImpl.class);

    @Override
    public Optional<Statistic> findById(UUID id) {
        String sql = "SELECT id, user_id, type, amount " +
                "FROM statistics WHERE id = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find statistic by id='{}'", id, e);
            throw new RuntimeException("Database error while finding statistic by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Statistic> findByUserIdAndType(UUID userId, StatisticType type) {
        String sql = "SELECT id, user_id, type, amount " +
                "FROM statistics WHERE user_id = ? AND type = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            ps.setString(2, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find statistic by userId='{}', type='{}'", userId, type, e);
            throw new RuntimeException("Database error while finding statistic by userId and type", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Statistic> findByUserId(UUID userId) {
        String sql = "SELECT id, user_id, type, amount " +
                "FROM statistics WHERE user_id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<Statistic> statistics = new ArrayList<>();
                while (rs.next()) {
                    statistics.add(mapRow(rs));
                }
                return statistics;
            }
        } catch (SQLException e) {
            log.error("Failed to find statistics by userId='{}'", userId, e);
            throw new RuntimeException("Database error while finding statistics by userId", e);
        }
    }

    @Override
    public void create(Statistic statistic) {
        String sql = "INSERT INTO statistics (id, user_id, type, amount) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statistic.getId().toString());
            ps.setString(2, statistic.getUserId().toString());
            ps.setString(3, statistic.getType().name());
            ps.setLong(4, statistic.getAmount());
            ps.executeUpdate();

            log.debug("Created statistic with id='{}', userId='{}', type='{}'", 
                    statistic.getId(), statistic.getUserId(), statistic.getType());
        } catch (SQLException e) {
            log.error("Failed to create statistic with id='{}'", statistic.getId(), e);
            throw new RuntimeException("Database error while creating statistic", e);
        }
    }

    @Override
    public void update(Statistic statistic) {
        String sql = "UPDATE statistics " +
                "SET amount = ? " +
                "WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, statistic.getAmount());
            ps.setString(2, statistic.getId().toString());
            ps.executeUpdate();

            log.debug("Updated statistic with id='{}', amount='{}'", 
                    statistic.getId(), statistic.getAmount());
        } catch (SQLException e) {
            log.error("Failed to update statistic with id='{}'", statistic.getId(), e);
            throw new RuntimeException("Database error while updating statistic", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM statistics WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();

            log.debug("Deleted statistic with id='{}'", id);
        } catch (SQLException e) {
            log.error("Failed to delete statistic with id='{}'", id, e);
            throw new RuntimeException("Database error while deleting statistic", e);
        }
    }

    private Statistic mapRow(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID userId = UUID.fromString(rs.getString("user_id"));
        StatisticType type = StatisticType.valueOf(rs.getString("type"));
        long amount = rs.getLong("amount");

        return new Statistic(id, userId, type, amount);
    }
}
