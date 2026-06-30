package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * Final route result rendered on the graph-map screen.
 */
public record GraphMapRouteResultResponse(
        boolean found,
        double totalCost,
        List<GraphMapGeoPointResponse> path
) {
}
