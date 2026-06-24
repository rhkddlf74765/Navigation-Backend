package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * Geographic point response in WGS84.
 */
public record GraphMapGeoPointResponse(
        double longitude,
        double latitude,
        double altitude
) {
}
