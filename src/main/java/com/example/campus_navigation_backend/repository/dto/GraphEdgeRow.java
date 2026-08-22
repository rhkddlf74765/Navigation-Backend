package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.MetricPoint;

import java.util.List;

public record GraphEdgeRow(
        long id,
        String highway,
        long source,
        long target,
        double distanceMeters,
        List<MetricPoint> geometry
) {
    public GraphEdgeRow {
        geometry =
                List.copyOf(
                        geometry
                );
    }
}