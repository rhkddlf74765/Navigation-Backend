package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.GraphNodeType;
import com.example.campus_navigation_backend.domain.graph.Point3D;

/**
 * 최종 노드 테이블에서 조회한 행이다.
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
