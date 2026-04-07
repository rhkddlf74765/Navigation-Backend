package com.example.campus_navigation_backend.visualizer;

public record Wgs84PointRow(
        long sequence,
        double longitude,
        double latitude,
        double altitude
) {
    public MapPoint toMapPoint() {
        return new MapPoint(latitude, longitude, altitude);
    }
}
