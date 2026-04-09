package com.triplify.infrastructure.repository.persistence;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Singleton
public class DatabaseMigrationInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationInitializer.class);
    private static final int CURRENT_SCHEMA_VERSION = 7;
    private static final List<MigrationStep> MIGRATIONS = List.of(
            new MigrationStep(1, "migrations/initial.sql"),
            new MigrationStep(2, "migrations/V2__countries_created_by_nullable.sql"),
            new MigrationStep(3, "seeders/country_seeder.sql"),
            new MigrationStep(4, "migrations/V3__trip_places_source_tracking.sql"),
            new MigrationStep(5, "migrations/V4__categories_created_by_nullable.sql"),
            new MigrationStep(6, "seeders/category_seeder.sql"),
            new MigrationStep(7, "migrations/V7__expand_color_palette.sql")
    );

    public void initialize() {
        Connection connection = SQLiteConnectionFactory.getConnection();

        try {
            int userVersion = readUserVersion(connection);
            if (userVersion >= CURRENT_SCHEMA_VERSION) {
                logger.info("SQLite schema already initialized; skipping migrations");
                return;
            }

            for (MigrationStep migration : MIGRATIONS) {
                if (migration.version() <= userVersion || migration.version() > CURRENT_SCHEMA_VERSION) {
                    continue;
                }

                logger.info("Applying SQLite migration v{} from {}", migration.version(), migration.fileName());
                runSqlFile(connection, migration.fileName());

                setUserVersion(connection, migration.version());
                userVersion = migration.version();
                logger.info("SQLite migration v{} applied", migration.version());
            }

            if (userVersion < CURRENT_SCHEMA_VERSION) {
                throw new IllegalStateException("Missing migration definition for schema version " + (userVersion + 1));
            }
        } catch (Exception e) {
            logger.error("Failed to initialize SQLite schema", e);
            throw new RuntimeException("Failed to initialize SQLite schema", e);
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

    private void runSqlFile(Connection connection, String fileName) throws IOException {
        String sqlScript = readSql(fileName);

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sqlScript);
        } catch (SQLException e) {
            logger.error("SQL execution failed for file: {}", fileName, e);
            throw new RuntimeException("Could not execute SQL file: " + fileName, e);
        }
    }

    private String readSql(String fileName) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                throw new IOException("SQL resource not found: " + fileName);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private record MigrationStep(int version, String fileName) {
    }
}
