package com.example.campus_navigation_backend.domain.projection;

import com.example.campus_navigation_backend.domain.graph.EdgePosition;
import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.graph.PhysicalEdge;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PointProjector {

    private static final double EPS = 1e-9;

    public EdgeProjection project(
            MetricPoint point,
            PhysicalEdge edge
    ) {
        List<MetricPoint> geometry =
                edge.geometry();

        ProjectionCandidate best =
                null;

        for (int i = 0;
             i < geometry.size() - 1;
             i++) {

            ProjectionCandidate candidate =
                    projectToSegment(
                            point,
                            geometry.get(i),
                            geometry.get(i + 1),
                            i
                    );

            if (best == null
                    || candidate.distanceMeters()
                    < best.distanceMeters()) {
                best = candidate;
            }
        }

        if (best == null) {
            throw new IllegalStateException(
                    "Projection candidate was not produced."
            );
        }

        EdgePosition position =
                edge.position(
                        best.segmentIndex(),
                        best.fraction()
                );

        return new EdgeProjection(
                edge,
                best.projectedPoint(),
                position,
                best.distanceMeters()
        );
    }

    private ProjectionCandidate
    projectToSegment(
            MetricPoint point,
            MetricPoint start,
            MetricPoint end,
            int segmentIndex
    ) {
        double dx =
                end.x() - start.x();

        double dy =
                end.y() - start.y();

        double lengthSquared =
                dx * dx + dy * dy;

        double fraction = 0.0;

        if (lengthSquared > EPS) {
            fraction =
                    (
                            (
                                    point.x()
                                            - start.x()
                            ) * dx
                                    +
                                    (
                                            point.y()
                                                    - start.y()
                                    ) * dy
                    )
                            / lengthSquared;

            fraction =
                    Math.max(
                            0.0,
                            Math.min(
                                    1.0,
                                    fraction
                            )
                    );
        }

        MetricPoint projected =
                new MetricPoint(
                        start.x()
                                + dx
                                * fraction,

                        start.y()
                                + dy
                                * fraction,

                        start.z()
                                + (
                                end.z()
                                        - start.z()
                        ) * fraction
                );

        return new ProjectionCandidate(
                segmentIndex,
                fraction,
                projected,
                point.distance2D(
                        projected
                )
        );
    }

    private record ProjectionCandidate(
            int segmentIndex,
            double fraction,
            MetricPoint projectedPoint,
            double distanceMeters
    ) {
    }
}