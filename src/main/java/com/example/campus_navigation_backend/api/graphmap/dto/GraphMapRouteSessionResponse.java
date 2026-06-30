package com.example.campus_navigation_backend.api.graphmap.dto;

import com.example.campus_navigation_backend.api.graphmap.GraphMapRouteSearchStatus;

import java.util.UUID;

/**
 * Route result snapshot for the graph-map screen.
 */
public record GraphMapRouteSessionResponse(
        UUID sessionId,
        GraphMapRouteSearchStatus status,
        String message,
        GraphMapRouteResultResponse result
) {
}
