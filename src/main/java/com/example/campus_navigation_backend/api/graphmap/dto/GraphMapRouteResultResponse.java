package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * Final route result returned when A* reaches the destination.
 */
public record GraphMapRouteResultResponse(
        boolean found,
        double totalCost,
        List<Long> pathNodeIds,
        List<GraphMapRouteStepResponse> steps
) {
}
