package com.example.campus_navigation_backend.domain.graph;

import java.util.List;

public record GraphEdge(
        long edgeId,
        long fromNodeId,
        long toNodeId,
        double distanceMeters,
        double cost,
        String edgeType,
        List<MetricPoint> geometry
) implements RoutingArc {

    private static final double EPS = 1e-9;

    public GraphEdge {
        if (!Double.isFinite(distanceMeters)
                || distanceMeters < 0.0) {
            throw new IllegalArgumentException(
                    "distanceMeters must be finite and non-negative."
            );
        }

        if (!Double.isFinite(cost)
                || cost < 0.0) {
            throw new IllegalArgumentException(
                    "cost must be finite and non-negative."
            );
        }

        if (cost + EPS < distanceMeters) {
            throw new IllegalArgumentException(
                    "cost must be greater than or equal to distanceMeters."
            );
        }

        geometry =
                List.copyOf(geometry);
    }

    @Override
    public RoutingArcKind kind() {
        return RoutingArcKind.BASE_EDGE;
    }
}