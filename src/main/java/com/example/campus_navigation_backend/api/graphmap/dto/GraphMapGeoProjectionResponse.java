package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * Projection preview in WGS84 coordinates.
 */
public record GraphMapGeoProjectionResponse(
        GraphMapGeoPointRequest inputPoint,
        GraphMapGeoPointResponse projectedPoint,
        long edgeFromNodeId,
        long edgeToNodeId,
        double distanceToEdge,
        double accessCostToFromNode,
        double accessCostToToNode
) {
}
