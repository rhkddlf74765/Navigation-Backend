package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * Point response used for projection previews and route snapshots.
 */
public record GraphMapPointResponse(double x, double y, double z) {
}
