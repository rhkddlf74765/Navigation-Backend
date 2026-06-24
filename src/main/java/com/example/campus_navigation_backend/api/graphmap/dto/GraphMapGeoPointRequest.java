package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * Geographic point used by the Leaflet graph map.
 */
public record GraphMapGeoPointRequest(
        double longitude,
        double latitude,
        Double altitude
) {
}
