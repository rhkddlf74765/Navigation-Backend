package com.example.campus_navigation_backend.domain.path;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;

import java.util.List;

public record PathResult (
        boolean found,
        double totalCost,
        List<GraphEdge> edges
) {
    public static PathResult success(double totalCost, List<GraphEdge> edges) {
        return new PathResult(true, totalCost, edges);
    }

    public static PathResult unreachable() {
        return new PathResult(false, Double.POSITIVE_INFINITY, List.of());
    }
}
