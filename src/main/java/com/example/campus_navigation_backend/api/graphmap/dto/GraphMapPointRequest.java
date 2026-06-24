package com.example.campus_navigation_backend.api.graphmap.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

/**
 * Map click point in the graph visualization coordinate system.
 */
public record GraphMapPointRequest(double x, double y, Double z) {

    public Point3D toPoint3D() {
        return new Point3D(x, y, z == null ? 0.0 : z);
    }
}
