package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * Graph edge snapshot used by the graph-map viewer.
 */
public record GraphMapEdgeResponse(
        long fromNodeId,
        long toNodeId,
        String highway,
        double cost,
        List<GraphMapPointResponse> geometry
) {
}
