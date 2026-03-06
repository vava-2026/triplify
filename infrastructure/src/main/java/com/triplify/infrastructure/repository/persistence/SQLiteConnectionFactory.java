package com.triplify.infrastructure.repository.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

public class SQLiteConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteConnectionFactory.class);
    private static final String URL = "jdbc:sqlite:triplify.db";

    private static Connection instance;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(SQLiteConnectionFactory::close));
    }

    private SQLiteConnectionFactory() {
    }

    public static synchronized Connection getConnection() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = DriverManager.getConnection(URL);
                enableSpatiaLite(instance);
                logger.info("SQLite connection established");
            }
            return instance;
        } catch (SQLException e) {
            logger.error("Failed to establish SQLite connection", e);
            throw new RuntimeException(e);
        }
    }

    private static synchronized void close() {
        if (instance != null) {
            try {
                instance.close();
                logger.info("SQLite connection closed");
            } catch (SQLException e) {
                logger.warn("Failed to close SQLite connection", e);
            } finally {
                instance = null;
            }
        }
    }

    private static void enableSpatiaLite(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            connection.unwrap(org.sqlite.SQLiteConnection.class)
                    .getDatabase()
                    .enable_load_extension(true);

            String libPath = extractLibrary();

            // Windows paths need backslashes escaped
            stmt.execute("SELECT load_extension('" + libPath.replace("\\", "\\\\") + "')");

            logger.info("SpatiaLite loaded successfully from: {}", libPath);
        } catch (Exception e) {
            logger.error("Failed to load SpatiaLite", e);
            throw new RuntimeException("SpatiaLite could not be loaded", e);
        }
    }

    private static String resolvePlatformFolder() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) return "/spatialite/windows-x86-64";
        if (os.contains("mac")) return "/spatialite/macos-arm64";
        if (os.contains("linux")) return "/spatialite/linux-x86-64";

        throw new RuntimeException("Unsupported OS: " + os);
    }

    private static String resolveLibraryName() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) return "mod_spatialite.dll";
        if (os.contains("mac")) return "mod_spatialite.dylib";
        if (os.contains("linux")) return "mod_spatialite.so";

        throw new RuntimeException("Unsupported OS: " + os);
    }

    // Windows: dependency DLLs must be pre-loaded via System.load() in order,
    // because Windows does not search the DLL's own directory for its dependencies.
    // mod_spatialite.dll must come last.
    private static final java.util.List<String> WINDOWS_DLL_LOAD_ORDER = java.util.List.of(
            "zlib1.dll",
            "libwinpthread-1.dll",
            "libgcc_s_seh-1.dll",
            "libstdc++-6.dll",
            "libiconv-2.dll",
            "libreadline8.dll",
            "libtermcap-0.dll",
            "liblzma-5.dll",
            "libcrypto-3-x64.dll",
            "libssl-3-x64.dll",
            "libexpat-1.dll",
            "libxml2.dll",
            "libjpeg-62.dll",
            "libsharpyuv-0.dll",
            "libwebp-7.dll",
            "libtiff-6.dll",
            "libzstd.dll",
            "libminizip-1.dll",
            "libfreexl-1.dll",
            "libsqlite3-0.dll",
            "libgeos.dll",
            "libgeos_c.dll",
            "libproj_9_2.dll",
            "librttopo-1.dll",
            "libcurl-4.dll",
            "mod_spatialite.dll"
    );

    private static String extractLibrary() throws IOException {
        String folder = resolvePlatformFolder();
        String libraryName = resolveLibraryName();

        // Create a single temp directory to hold all platform files
        Path tempDir = Files.createTempDirectory("spatialite");
        tempDir.toFile().deleteOnExit();

        // List all resources inside the platform folder using the classpath
        URI folderUri;
        try {
            var resource = SQLiteConnectionFactory.class.getResource(folder);
            if (resource == null) throw new IOException("Resource folder not found on classpath: " + folder);
            folderUri = resource.toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Could not resolve resource folder URI: " + folder, e);
        }

        // Support both plain directories (IDE / exploded) and JARs
        FileSystem jarFs = null;
        Path resourceDir;
        if (folderUri.getScheme().equals("jar")) {
            jarFs = FileSystems.newFileSystem(folderUri, Collections.emptyMap());
            resourceDir = jarFs.getPath(folder);
        } else {
            resourceDir = Path.of(folderUri);
        }

        try (var stream = Files.list(resourceDir)) {
            for (Path entry : stream.toList()) {
                String fileName = entry.getFileName().toString();
                Path dest = tempDir.resolve(fileName);
                try (var in = SQLiteConnectionFactory.class.getResourceAsStream(folder + "/" + fileName)) {
                    if (in == null) continue;
                    Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                    dest.toFile().deleteOnExit();
                }
            }
        } finally {
            if (jarFs != null) jarFs.close();
        }

        // On Windows, pre-load each DLL in dependency order via System.load()
        // so the JVM's loaded-library table satisfies all transitive dependencies
        // when mod_spatialite.dll is later loaded by SQLite's load_extension.
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            for (String dll : WINDOWS_DLL_LOAD_ORDER) {
                Path dllPath = tempDir.resolve(dll);
                if (Files.exists(dllPath)) {
                    System.load(dllPath.toAbsolutePath().toString());
                    logger.debug("Pre-loaded Windows DLL: {}", dll);
                }
            }
        }

        return tempDir.resolve(libraryName).toAbsolutePath().toString();
    }
}
