package com.triplify.infrastructure.repository.persistence;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class DatabaseMigrationInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationInitializer.class);
    private static final int BASELINE_SCHEMA_VERSION = 1;
    private static final String[] SEEDERS = {
            "seeders/user_seeder.sql",
            "seeders/country_seeder.sql",
            "seeders/category_seeder.sql",
            "seeders/badge_group_seeder.sql",
            "seeders/badge_image_seeder.sql",
            "seeders/badge_seeder.sql"
    };

    public void initialize() {
        Connection connection = SQLiteConnectionFactory.getConnection();

        try {
            int userVersion = readUserVersion(connection);
            if (userVersion >= BASELINE_SCHEMA_VERSION) {
                logger.info("SQLite schema already initialized; skipping baseline migration");
                return;
            }

            logger.info("Running baseline SQLite migration");
            runBaselineMigration(connection);
            setUserVersion(connection, BASELINE_SCHEMA_VERSION);
            logger.info("Baseline SQLite migration finished");
        } catch (Exception e) {
            logger.error("Failed to initialize SQLite schema", e);
            throw new RuntimeException("Failed to initialize SQLite schema", e);
        }

        try {
            runSeeders(connection, SEEDERS);
        }
        catch (Exception e) {
            logger.error("Failed to run seeders", e);
            throw new RuntimeException("Failed to run seeders", e);
        }
    }

    private int readUserVersion(Connection connection) throws SQLException {
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery("PRAGMA user_version")) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private void setUserVersion(Connection connection, int version) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA user_version = " + version);
        }
    }

    private void runBaselineMigration(Connection connection) throws IOException, SQLException {
        String sqlScript = readMigrationSql();

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sqlScript);
        } catch (SQLException e) {
            logger.error("Migration failed. Please check the SQL script syntax", e);
            throw new RuntimeException("Could not execute migration SQL", e);
        }
    }

    private void executeStatement(Statement stmt, String sql) throws SQLException {
        logger.info("Executing migration SQL: {}", sql);
        stmt.execute(sql);
    }

    private String readMigrationSql() throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream("sqlite_migrations.sql")) {
            if (stream == null) {
                throw new IOException("Migration resource not found: sqlite_migrations.sql");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void runSeeders(Connection connection, String[] seederFiles) throws IOException, SQLException {
        for (String seederFile : seederFiles) {
            String sqlScript = readSeedersSql(seederFile);
            try (Statement stmt = connection.createStatement()) {
                executeStatement(stmt, sqlScript);
            } catch (SQLException e) {
                logger.error("Seeder execution failed for file: {}. Please check the SQL script syntax", seederFile, e);
                throw new RuntimeException("Could not execute seeder SQL from file: " + seederFile, e);
            }
        }
    }

    private String readSeedersSql(String fileName) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                logger.error("Seeders resource not found: {}", fileName);
                throw new IOException("Seeders resource not found: " + fileName);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
