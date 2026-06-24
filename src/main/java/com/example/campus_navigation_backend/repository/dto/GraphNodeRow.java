package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.GraphNodeType;
import com.example.campus_navigation_backend.domain.graph.Point3D;

/**
 * Row loaded from final_nodes_3d.
 */
public record GraphNodeRow(
        long id,
        String nodeType,
        String description,
        double x,
        double y,
        double z
) {
    public Point3D point() {
        return new Point3D(x, y, z);
    }

    public GraphNodeType graphNodeType() {
        if (nodeType == null) {
            return GraphNodeType.BASE;
        }
        return switch (nodeType.trim().toLowerCase()) {
            case "intersection" -> GraphNodeType.INTERSECTION;
            case "entrance" -> GraphNodeType.ENTRANCE;
            default -> GraphNodeType.BASE;
        };
    }
}
