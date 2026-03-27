package com.triplify.ui.map;

import java.util.List;

public final class CountryBoundary {

    private final String name;
    private final List<List<LonLat>> rings;
    private final double minLongitude;
    private final double maxLongitude;
    private final double minLatitude;
    private final double maxLatitude;

    public CountryBoundary(String name, List<List<LonLat>> rings) {
        this.name = name;
        this.rings = List.copyOf(rings);

        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;

        for (List<LonLat> ring : rings) {
            for (LonLat point : ring) {
                minLon = Math.min(minLon, point.longitude());
                maxLon = Math.max(maxLon, point.longitude());
                minLat = Math.min(minLat, point.latitude());
                maxLat = Math.max(maxLat, point.latitude());
            }
        }

        this.minLongitude = minLon;
        this.maxLongitude = maxLon;
        this.minLatitude = minLat;
        this.maxLatitude = maxLat;
    }

    public String name() {
        return name;
    }

    public List<List<LonLat>> rings() {
        return rings;
    }

    public boolean contains(double latitude, double longitude) {
        if (longitude < minLongitude || longitude > maxLongitude
                || latitude < minLatitude || latitude > maxLatitude) {
            return false;
        }

        for (List<LonLat> ring : rings) {
            if (pointInRing(longitude, latitude, ring)) {
                return true;
            }
        }

        return false;
    }

    private boolean pointInRing(double longitude, double latitude, List<LonLat> ring) {
        boolean inside = false;
        for (int i = 0, j = ring.size() - 1; i < ring.size(); j = i++) {
            LonLat current = ring.get(i);
            LonLat previous = ring.get(j);

            boolean intersects = ((current.latitude() > latitude) != (previous.latitude() > latitude))
                    && (longitude < ((previous.longitude() - current.longitude())
                    * (latitude - current.latitude())
                    / ((previous.latitude() - current.latitude()) + 1e-12))
                    + current.longitude());

            if (intersects) {
                inside = !inside;
            }
        }
        return inside;
    }

    public record LonLat(double longitude, double latitude) {
    }
}
