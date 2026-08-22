package com.example.campus_navigation_backend.domain.path;

import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.graph.RoutingArc;
import com.example.campus_navigation_backend.domain.graph.RoutingArcKind;

import java.util.ArrayList;
import java.util.List;

public record PathResult(
        boolean found,
        double totalCost,
        double totalDistanceMeters,
        List<RoutingArc> arcs
) {

    private static final double
            DUPLICATE_POINT_TOLERANCE =
            1e-4;

    public PathResult {
        arcs =
                List.copyOf(arcs);
    }

    public static PathResult success(
            List<? extends RoutingArc> arcs
    ) {
        List<RoutingArc> immutable =
                List.copyOf(arcs);

        double totalCost =
                immutable.stream()
                        .mapToDouble(
                                RoutingArc::cost
                        )
                        .sum();

        double totalDistance =
                immutable.stream()
                        .mapToDouble(
                                RoutingArc
                                        ::distanceMeters
                        )
                        .sum();

        return new PathResult(
                true,
                totalCost,
                totalDistance,
                immutable
        );
    }

    public static PathResult unreachable() {
        return new PathResult(
                false,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                List.of()
        );
    }

    public double approachDistanceMeters() {
        return arcs.stream()
                .filter(
                        arc ->
                                arc.kind()
                                        == RoutingArcKind
                                        .CONNECTOR
                )
                .mapToDouble(
                        RoutingArc
                                ::distanceMeters
                )
                .sum();
    }

    public double approachCost() {
        return arcs.stream()
                .filter(
                        arc ->
                                arc.kind()
                                        == RoutingArcKind
                                        .CONNECTOR
                )
                .mapToDouble(
                        RoutingArc::cost
                )
                .sum();
    }

    public double graphDistanceMeters() {
        return totalDistanceMeters
                - approachDistanceMeters();
    }

    public double graphCost() {
        return totalCost
                - approachCost();
    }

    public List<MetricPoint>
    pathPoints() {
        List<MetricPoint> points =
                new ArrayList<>();

        for (RoutingArc arc : arcs) {
            for (MetricPoint point
                    : arc.geometry()) {

                if (points.isEmpty()
                        || points
                        .get(
                                points.size() - 1
                        )
                        .distance3D(
                                point
                        )
                        >
                        DUPLICATE_POINT_TOLERANCE) {

                    points.add(point);
                }
            }
        }

        return List.copyOf(points);
    }
}