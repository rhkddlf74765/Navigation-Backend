package com.example.campus_navigation_backend.domain.graph;

public record GraphNode(
        long id,
        GraphNodeType type,
        long sourceId,
        Point3D point
) {
}
