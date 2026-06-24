package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * Graph edge converted to WGS84 for map rendering.
 */
public record GraphMapGeoEdgeResponse(
        long fromNodeId,
        long toNodeId,
        String highway,
        double cost,
        List<GraphMapGeoPointResponse> geometry
) {
}
