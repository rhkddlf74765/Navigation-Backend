package com.example.campus_navigation_backend.visualizer;

public record Wgs84PointRow(
        long sequence,
        double lon,
        double lat,
        double ele
) {
    public MapPoint toMapPoint() {
        return new MapPoint(lat, lon, ele);
    }
}
