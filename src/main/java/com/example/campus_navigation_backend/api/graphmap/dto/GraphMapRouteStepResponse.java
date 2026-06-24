package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * One edge traversal in the computed route.
 */
public record GraphMapRouteStepResponse(
        long fromNodeId,
        long toNodeId,
        double cost
) {
}
