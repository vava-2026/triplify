package com.triplify.infrastructure.unsplash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// Thin HTTP client for the Unsplash Search Photos API
public class UnsplashClient {

    private static final Logger log = LoggerFactory.getLogger(UnsplashClient.class);

    private static final String API_BASE     = "https://api.unsplash.com";
    private static final String ENV_KEY      = "UNSPLASH_ACCESS_KEY";
    private static final String PROPERTY_KEY = "triplify.unsplash.key";

    private final HttpClient http;
    private final String accessKey;

    public UnsplashClient() {
        this.accessKey = resolveAccessKey();
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Searches Unsplash for a landscape photo matching {@code query},
     * downloads the image bytes into a temp file, and returns its path.
     *
     * @return path to the downloaded temp file, or empty if nothing was found / download failed
     */
    public Optional<Path> downloadPhoto(String query) {
        Optional<PhotoMeta> meta = searchPhoto(query);
        if (meta.isEmpty()) return Optional.empty();
        return downloadToTemp(meta.get());
    }

    // Private help methods
    private Optional<PhotoMeta> searchPhoto(String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = API_BASE + "/search/photos?query=" + encoded
                + "&per_page=3&orientation=landscape&content_filter=high";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Client-ID " + accessKey)
                .header("Accept-Version", "v1")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Unsplash API returned {} for query '{}'", response.statusCode(), query);
                return Optional.empty();
            }

            JsonObject body    = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray  results = body.getAsJsonArray("results");

            if (results == null || results.isEmpty()) {
                log.debug("No Unsplash results for query '{}'", query);
                return Optional.empty();
            }

            JsonObject photo            = results.get(0).getAsJsonObject();
            String     photoUrl         = photo.getAsJsonObject("urls").get("regular").getAsString();
            String     downloadLocation = photo.getAsJsonObject("links").get("download_location").getAsString();

            triggerDownload(downloadLocation); // required by Unsplash ToS

            return Optional.of(new PhotoMeta(photoUrl, query));

        } catch (IOException | InterruptedException e) {
            log.error("Unsplash search failed for query '{}'", query, e);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private Optional<Path> downloadToTemp(PhotoMeta meta) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(meta.photoUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    http.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                log.warn("Photo download failed, status={}, url='{}'", response.statusCode(), meta.photoUrl());
                return Optional.empty();
            }

            String contentType = response.headers().firstValue("Content-Type").orElse("image/jpeg");
            String extension   = extensionFromContentType(contentType);

            Path tempFile = Files.createTempFile("triplify-unsplash-", "." + extension);
            try (InputStream in = response.body()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.debug("Downloaded photo for '{}' → '{}'", meta.query(), tempFile.getFileName());
            return Optional.of(tempFile);

        } catch (IOException | InterruptedException e) {
            log.error("Photo download failed for query '{}'", meta.query(), e);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /** Required by Unsplash ToS whenever a photo is used. */
    private void triggerDownload(String downloadLocation) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(downloadLocation + "?client_id=" + accessKey))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try {
            http.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            log.warn("Could not trigger Unsplash download tracking");
            Thread.currentThread().interrupt();
        }
    }

    private static String extensionFromContentType(String contentType) {
        return switch (contentType.split(";")[0].trim().toLowerCase(Locale.ROOT)) {
            case "image/png"  -> "png";
            case "image/gif"  -> "gif";
            case "image/webp" -> "webp";
            default           -> "jpg";
        };
    }

    private static String resolveAccessKey() {
        String key = System.getProperty(PROPERTY_KEY);
        if (key == null || key.isBlank()) key = System.getenv(ENV_KEY);
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                "Unsplash access key not configured. " +
                "Set '" + ENV_KEY + "' env variable or '-D" + PROPERTY_KEY + "' JVM property."
            );
        }
        return key.trim();
    }

    private record PhotoMeta(String photoUrl, String query) {}
}
