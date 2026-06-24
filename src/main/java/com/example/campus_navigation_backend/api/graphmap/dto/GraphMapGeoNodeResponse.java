package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * Graph node converted to WGS84 for map rendering.
 */
public record GraphMapGeoNodeResponse(
        long id,
        String nodeType,
        String description,
        double longitude,
        double latitude,
        double altitude
) {
}
