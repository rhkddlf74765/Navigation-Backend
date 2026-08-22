package com.example.campus_navigation_backend.application.routing;

import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.projection.EdgeProjection;

import java.util.List;

public sealed interface
ResolvedRouteEndpoint
        permits
        ResolvedRouteEndpoint.Building,
        ResolvedRouteEndpoint.Coordinate {

    String displayName();

    record Building(
            String displayName,
            List<Long> entranceNodeIds
    ) implements ResolvedRouteEndpoint {

        public Building {
            entranceNodeIds =
                    List.copyOf(
                            entranceNodeIds
                    );

            if (entranceNodeIds.isEmpty()) {
                throw new IllegalArgumentException(
                        "Building must have at least one entrance."
                );
            }
        }
    }

    record Coordinate(
            MetricPoint point,
            List<EdgeProjection> projections
    ) implements ResolvedRouteEndpoint {

        public Coordinate {
            projections =
                    List.copyOf(
                            projections
                    );

            if (projections.isEmpty()) {
                throw new IllegalArgumentException(
                        "Coordinate must have at least one projection."
                );
            }
        }

        @Override
        public String displayName() {
            return null;
        }
    }
}
