package com.example.campus_navigation_backend.domain.geo;

public record GeoPoint(
        double lon,
        double lat,
        double ele
) {
    public GeoPoint {
        if (!Double.isFinite(lon)
                || lon < -180.0
                || lon > 180.0) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180."
            );
        }

        if (!Double.isFinite(lat)
                || lat < -90.0
                || lat > 90.0) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90."
            );
        }

        if (!Double.isFinite(ele)) {
            throw new IllegalArgumentException(
                    "Elevation must be finite."
            );
        }
    }
}