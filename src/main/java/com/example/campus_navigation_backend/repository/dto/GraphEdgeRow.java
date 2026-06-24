package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

/**
 * Row loaded from final_edges_split_3d.
 */
public record GraphEdgeRow(
        long id,
        long originalEdgeId,
        String highway,
        long source,
        long target,
        double cost,
        List<Point3D> geometry
) {
}
