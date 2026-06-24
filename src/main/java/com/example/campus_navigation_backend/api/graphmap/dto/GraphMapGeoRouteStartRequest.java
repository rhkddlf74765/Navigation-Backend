package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * Route debug request using geographic coordinates.
 */
public record GraphMapGeoRouteStartRequest(
        GraphMapGeoPointRequest startPoint,
        GraphMapGeoPointRequest destinationPoint
) {
}
