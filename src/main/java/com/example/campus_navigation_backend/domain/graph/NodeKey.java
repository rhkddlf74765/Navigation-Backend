package com.example.campus_navigation_backend.domain.graph;

public record NodeKey(
        long x, long y
) {
    public static NodeKey from(Point3D point) {
        return new NodeKey(
                Math.round(point.x() * 1000),
                Math.round(point.y() * 1000)
        );
    }
}
