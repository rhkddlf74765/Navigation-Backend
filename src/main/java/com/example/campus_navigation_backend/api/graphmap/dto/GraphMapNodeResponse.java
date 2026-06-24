package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * Graph node snapshot used by the graph-map viewer.
 */
public record GraphMapNodeResponse(long id, double x, double y, double z) {
}
