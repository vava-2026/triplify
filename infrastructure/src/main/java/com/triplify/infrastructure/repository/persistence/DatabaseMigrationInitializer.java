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

    private void runBaselineMigration(Connection connection) throws IOException {
        String sql = readMigrationSql();
        try {
            connection.unwrap(SQLiteConnection.class)
                    .getDatabase()
                    .exec(sql, true);
        } catch (Exception e) {
            throw new RuntimeException("Could not execute migration SQL", e);
        }
    }

    private String readMigrationSql() throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream("sqlite_migrations.sql")) {
            if (stream == null) {
                throw new IOException("Migration resource not found: sqlite_migrations.sql");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
