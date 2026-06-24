package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * Route debug request containing the start and destination points.
 */
public record GraphMapRouteStartRequest(
        GraphMapPointRequest startPoint,
        GraphMapPointRequest destinationPoint
) {
}
