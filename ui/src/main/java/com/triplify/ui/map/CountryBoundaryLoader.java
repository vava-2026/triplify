package com.triplify.ui.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public final class CountryBoundaryLoader {

    private static final String RESOURCE_PATH = "/com/triplify/ui/map/countries.geo.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CountryBoundaryLoader() {
    }

    public static List<CountryBoundary> load() {
        try (InputStream inputStream = CountryBoundaryLoader.class.getResourceAsStream(RESOURCE_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Country boundaries resource not found: " + RESOURCE_PATH);
            }

            JsonNode root = OBJECT_MAPPER.readTree(inputStream);
            JsonNode features = root.path("features");
            List<CountryBoundary> boundaries = new ArrayList<>();

            for (JsonNode feature : features) {
                String name = feature.path("properties").path("name").asText(null);
                JsonNode geometry = feature.path("geometry");
                String geometryType = geometry.path("type").asText();

                if (name == null || name.isBlank()) {
                    continue;
                }

                List<List<CountryBoundary.LonLat>> rings = switch (geometryType) {
                    case "Polygon" -> parsePolygon(geometry.path("coordinates"));
                    case "MultiPolygon" -> parseMultiPolygon(geometry.path("coordinates"));
                    default -> List.of();
                };

                if (!rings.isEmpty()) {
                    boundaries.add(new CountryBoundary(name, rings));
                }
            }

            return List.copyOf(boundaries);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load country boundaries.", exception);
        }
    }

    private static List<List<CountryBoundary.LonLat>> parsePolygon(JsonNode polygonCoordinates) {
        if (!polygonCoordinates.isArray() || polygonCoordinates.isEmpty()) {
            return List.of();
        }

        List<CountryBoundary.LonLat> outerRing = parseRing(polygonCoordinates.get(0));
        return outerRing.isEmpty() ? List.of() : List.of(outerRing);
    }

    private static List<List<CountryBoundary.LonLat>> parseMultiPolygon(JsonNode multiPolygonCoordinates) {
        List<List<CountryBoundary.LonLat>> rings = new ArrayList<>();

        for (JsonNode polygonCoordinates : multiPolygonCoordinates) {
            List<List<CountryBoundary.LonLat>> polygon = parsePolygon(polygonCoordinates);
            rings.addAll(polygon);
        }

        return rings;
    }

    private static List<CountryBoundary.LonLat> parseRing(JsonNode ringCoordinates) {
        List<CountryBoundary.LonLat> ring = new ArrayList<>();
        for (JsonNode coordinate : ringCoordinates) {
            if (coordinate.size() < 2) {
                continue;
            }
            ring.add(new CountryBoundary.LonLat(
                    coordinate.get(0).asDouble(),
                    coordinate.get(1).asDouble()
            ));
        }
        return List.copyOf(ring);
    }
}
