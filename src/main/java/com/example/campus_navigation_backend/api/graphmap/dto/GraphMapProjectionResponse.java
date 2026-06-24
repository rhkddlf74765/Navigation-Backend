package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * Projection result for a clicked point and its nearest edge.
 */
public record GraphMapProjectionResponse(
        GraphMapPointRequest inputPoint,
        GraphMapPointResponse projectedPoint,
        long edgeFromNodeId,
        long edgeToNodeId,
        double distanceToEdge,
        double accessCostToFromNode,
        double accessCostToToNode
) {
}
