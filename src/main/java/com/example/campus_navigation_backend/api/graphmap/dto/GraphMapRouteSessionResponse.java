package com.example.campus_navigation_backend.api.graphmap.dto;

import com.example.campus_navigation_backend.api.graphmap.GraphMapRouteSearchStatus;

import java.util.List;
import java.util.UUID;

/**
 * Current route search snapshot for polling-based progress updates.
 */
public record GraphMapRouteSessionResponse(
        UUID sessionId,
        GraphMapRouteSearchStatus status,
        GraphMapProjectionResponse startProjection,
        GraphMapProjectionResponse destinationProjection,
        long visitedCount,
        int frontierSize,
        Long currentNodeId,
        List<Long> visitedNodeIds,
        List<Long> frontierNodeIds,
        String message,
        GraphMapRouteResultResponse result
) {
}
