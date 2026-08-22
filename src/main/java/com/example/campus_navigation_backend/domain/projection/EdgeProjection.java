package com.example.campus_navigation_backend.domain.projection;

import com.example.campus_navigation_backend.domain.graph.EdgePosition;
import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.graph.PhysicalEdge;

public record EdgeProjection(
        PhysicalEdge edge,
        MetricPoint projectedPoint,
        EdgePosition position,
        double connectorDistanceMeters
) {
}
