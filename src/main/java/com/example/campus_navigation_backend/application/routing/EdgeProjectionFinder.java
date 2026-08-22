package com.example.campus_navigation_backend.application.routing;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.graph.PhysicalEdge;
import com.example.campus_navigation_backend.domain.projection.EdgeProjection;
import com.example.campus_navigation_backend.domain.projection.PointProjector;
import com.example.campus_navigation_backend.infrastructure.spatial.EdgeSpatialIndex;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class EdgeProjectionFinder {

    private final NavigationProperties properties;
    private final EdgeSpatialIndex edgeSpatialIndex;
    private final PointProjector pointProjector;

    public EdgeProjectionFinder(
            NavigationProperties properties,
            EdgeSpatialIndex edgeSpatialIndex,
            PointProjector pointProjector
    ) {
        this.properties = properties;
        this.edgeSpatialIndex =
                edgeSpatialIndex;
        this.pointProjector =
                pointProjector;
    }

    public List<EdgeProjection>
    findCandidates(
            MetricPoint point
    ) {
        double radius =
                properties
                        .projectionRadiusMeters();

        int limit =
                Math.max(
                        1,
                        properties
                                .projectionMaxCandidates()
                );

        List<PhysicalEdge> nearbyEdges =
                edgeSpatialIndex.query(
                        point,
                        radius
                );

        List<EdgeProjection> candidates =
                nearbyEdges.stream()
                        .map(
                                edge ->
                                        pointProjector
                                                .project(
                                                        point,
                                                        edge
                                                )
                        )
                        .filter(
                                projection ->
                                        projection
                                                .connectorDistanceMeters()
                                                <= radius
                        )
                        .sorted(
                                Comparator
                                        .comparingDouble(
                                                EdgeProjection
                                                        ::connectorDistanceMeters
                                        )
                        )
                        .limit(limit)
                        .toList();

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "No routable edge exists within "
                            + radius
                            + " meters."
            );
        }

        return candidates;
    }
}
