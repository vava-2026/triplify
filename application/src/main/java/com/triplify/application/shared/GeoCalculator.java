package com.triplify.application.shared;

// Partly generated with copilot
public final class GeoCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoCalculator() {}

    public static double distanceKm(Double fromLatitude, Double fromLongitude, Double toLatitude, Double toLongitude) {
        if (fromLatitude == null || fromLongitude == null || toLatitude == null || toLongitude == null) {
            return 0.0;
        }

        double latDistance = Math.toRadians(toLatitude - fromLatitude);
        double lonDistance = Math.toRadians(toLongitude - fromLongitude);
        double startLat = Math.toRadians(fromLatitude);
        double endLat = Math.toRadians(toLatitude);

        double a = Math.pow(Math.sin(latDistance / 2), 2)
                + Math.cos(startLat) * Math.cos(endLat) * Math.pow(Math.sin(lonDistance / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
