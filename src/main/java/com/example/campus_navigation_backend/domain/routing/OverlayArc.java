package com.example.campus_navigation_backend.domain.routing;

import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.graph.RoutingArc;
import com.example.campus_navigation_backend.domain.graph.RoutingArcKind;

import java.util.List;

public record OverlayArc(
        long fromNodeId,
        long toNodeId,
        double distanceMeters,
        double cost,
        List<MetricPoint> geometry,
        RoutingArcKind kind
) implements RoutingArc {

    private static final double EPS = 1e-9;

    public OverlayArc {
        if (!Double.isFinite(
                distanceMeters
        )
                || distanceMeters < 0.0) {
            throw new IllegalArgumentException(
                    "Invalid distance."
            );
        }

        if (!Double.isFinite(cost)
                || cost < 0.0) {
            throw new IllegalArgumentException(
                    "Invalid cost."
            );
        }

        if (cost + EPS < distanceMeters) {
            throw new IllegalArgumentException(
                    "cost must be >= distance."
            );
        }

        geometry =
                geometry == null
                        ? List.of()
                        : List.copyOf(
                        geometry
                );
    }
}
