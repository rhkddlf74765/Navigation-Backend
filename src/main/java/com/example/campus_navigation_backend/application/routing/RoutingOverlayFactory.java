package com.example.campus_navigation_backend.application.routing;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.EdgePosition;
import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.graph.PhysicalEdge;
import com.example.campus_navigation_backend.domain.graph.RoutingArc;
import com.example.campus_navigation_backend.domain.graph.RoutingArcKind;
import com.example.campus_navigation_backend.domain.projection.EdgeProjection;
import com.example.campus_navigation_backend.domain.routing.OverlayArc;
import com.example.campus_navigation_backend.domain.routing.RoutingOverlay;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RoutingOverlayFactory {

    private static final long
            SUPER_SOURCE_ID = -1L;

    private static final long
            SUPER_DESTINATION_ID = -2L;

    private static final double EPS = 1e-9;

    private final CampusGraphStore
            campusGraphStore;

    public RoutingOverlayFactory(
            CampusGraphStore
                    campusGraphStore
    ) {
        this.campusGraphStore =
                campusGraphStore;
    }

    public RoutingOverlay create(
            ResolvedRouteRequest request
    ) {
        CampusGraph baseGraph =
                campusGraphStore.graph();

        Builder builder =
                new Builder(
                        baseGraph
                );

        addStartEndpoint(
                builder,
                request.start()
        );

        addDestinationEndpoint(
                builder,
                request.destination()
        );

        builder
                .connectProjectionFragments();

        return builder.build();
    }

    private void addStartEndpoint(
            Builder builder,
            ResolvedRouteEndpoint endpoint
    ) {
        if (endpoint
                instanceof
                ResolvedRouteEndpoint
                        .Building building) {

            for (long entranceNodeId
                    : building
                    .entranceNodeIds()) {

                MetricPoint entrancePoint =
                        campusGraphStore
                                .getNode(
                                        entranceNodeId
                                )
                                .point();

                builder.addArc(
                        new OverlayArc(
                                SUPER_SOURCE_ID,
                                entranceNodeId,
                                0.0,
                                0.0,
                                List.of(
                                        entrancePoint
                                ),
                                RoutingArcKind
                                        .VIRTUAL
                        )
                );
            }

            return;
        }

        ResolvedRouteEndpoint.Coordinate
                coordinate =
                (ResolvedRouteEndpoint.Coordinate)
                        endpoint;

        for (EdgeProjection projection
                : coordinate.projections()) {

            long projectionNodeId =
                    builder.addProjection(
                            projection
                    );

            builder.addArc(
                    new OverlayArc(
                            SUPER_SOURCE_ID,
                            projectionNodeId,

                            projection
                                    .connectorDistanceMeters(),

                            projection
                                    .connectorDistanceMeters(),

                            List.of(
                                    coordinate.point(),
                                    projection
                                            .projectedPoint()
                            ),

                            RoutingArcKind
                                    .CONNECTOR
                    )
            );
        }
    }

    private void addDestinationEndpoint(
            Builder builder,
            ResolvedRouteEndpoint endpoint
    ) {
        if (endpoint
                instanceof
                ResolvedRouteEndpoint
                        .Building building) {

            for (long entranceNodeId
                    : building
                    .entranceNodeIds()) {

                MetricPoint entrancePoint =
                        campusGraphStore
                                .getNode(
                                        entranceNodeId
                                )
                                .point();

                builder
                        .addDestinationPoint(
                                entrancePoint
                        );

                builder.addArc(
                        new OverlayArc(
                                entranceNodeId,
                                SUPER_DESTINATION_ID,
                                0.0,
                                0.0,
                                List.of(
                                        entrancePoint
                                ),
                                RoutingArcKind
                                        .VIRTUAL
                        )
                );
            }

            return;
        }

        ResolvedRouteEndpoint.Coordinate
                coordinate =
                (ResolvedRouteEndpoint.Coordinate)
                        endpoint;

        builder.addDestinationPoint(
                coordinate.point()
        );

        for (EdgeProjection projection
                : coordinate.projections()) {

            long projectionNodeId =
                    builder.addProjection(
                            projection
                    );

            builder.addArc(
                    new OverlayArc(
                            projectionNodeId,
                            SUPER_DESTINATION_ID,

                            projection
                                    .connectorDistanceMeters(),

                            projection
                                    .connectorDistanceMeters(),

                            List.of(
                                    projection
                                            .projectedPoint(),
                                    coordinate.point()
                            ),

                            RoutingArcKind
                                    .CONNECTOR
                    )
            );
        }
    }

    private static final class Builder {

        private final CampusGraph baseGraph;

        private long nextVirtualNodeId =
                -3L;

        private final
        Map<Long, MetricPoint>
                virtualPoints =
                new HashMap<>();

        private final
        Map<Long, List<RoutingArc>>
                addedAdjacency =
                new HashMap<>();

        private final
        Map<Long, List<ProjectionNode>>
                projectionsByEdgeId =
                new LinkedHashMap<>();

        private final List<MetricPoint>
                destinationPoints =
                new ArrayList<>();

        private Builder(
                CampusGraph baseGraph
        ) {
            this.baseGraph =
                    baseGraph;
        }

        private long addProjection(
                EdgeProjection projection
        ) {
            long nodeId =
                    nextVirtualNodeId--;

            virtualPoints.put(
                    nodeId,
                    projection
                            .projectedPoint()
            );

            projectionsByEdgeId
                    .computeIfAbsent(
                            projection
                                    .edge()
                                    .id(),

                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            new ProjectionNode(
                                    nodeId,
                                    projection
                            )
                    );

            return nodeId;
        }

        private void addArc(
                RoutingArc arc
        ) {
            addedAdjacency
                    .computeIfAbsent(
                            arc.fromNodeId(),
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(arc);
        }

        private void addDestinationPoint(
                MetricPoint point
        ) {
            destinationPoints.add(
                    point
            );
        }

        private void
        connectProjectionFragments() {

            for (List<ProjectionNode>
                    projectionNodes
                    : projectionsByEdgeId
                    .values()) {

                if (projectionNodes.isEmpty()) {
                    continue;
                }

                PhysicalEdge edge =
                        projectionNodes
                                .get(0)
                                .projection()
                                .edge();

                List<EdgeNodePosition>
                        ordered =
                        new ArrayList<>();

                ordered.add(
                        new EdgeNodePosition(
                                edge.sourceNodeId(),
                                edge.sourcePosition()
                        )
                );

                projectionNodes
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                node ->
                                                        node
                                                                .projection()
                                                                .position()
                                        )
                        )
                        .forEach(
                                node ->
                                        ordered.add(
                                                new EdgeNodePosition(
                                                        node.nodeId(),
                                                        node.projection()
                                                                .position()
                                                )
                                        )
                        );

                ordered.add(
                        new EdgeNodePosition(
                                edge.targetNodeId(),
                                edge.targetPosition()
                        )
                );

                for (int i = 0;
                     i < ordered.size() - 1;
                     i++) {

                    EdgeNodePosition left =
                            ordered.get(i);

                    EdgeNodePosition right =
                            ordered.get(i + 1);

                    double distance =
                            edge.distanceBetween(
                                    left.position(),
                                    right.position()
                            );

                    double forwardCost =
                            edge.forwardCostBetween(
                                    left.position(),
                                    right.position()
                            );

                    double reverseCost =
                            edge.reverseCostBetween(
                                    right.position(),
                                    left.position()
                            );

                    forwardCost =
                            Math.max(
                                    distance,
                                    forwardCost
                            );

                    reverseCost =
                            Math.max(
                                    distance,
                                    reverseCost
                            );

                    if (distance <= EPS) {
                        forwardCost = 0.0;
                        reverseCost = 0.0;
                    }

                    addArc(
                            new OverlayArc(
                                    left.nodeId(),
                                    right.nodeId(),
                                    distance,
                                    forwardCost,
                                    edge.slice(
                                            left.position(),
                                            right.position()
                                    ),
                                    RoutingArcKind
                                            .EDGE_FRAGMENT
                            )
                    );

                    addArc(
                            new OverlayArc(
                                    right.nodeId(),
                                    left.nodeId(),
                                    distance,
                                    reverseCost,
                                    edge.slice(
                                            right.position(),
                                            left.position()
                                    ),
                                    RoutingArcKind
                                            .EDGE_FRAGMENT
                            )
                    );
                }
            }
        }

        private RoutingOverlay build() {
            if (destinationPoints.isEmpty()) {
                throw new IllegalStateException(
                        "Routing overlay has no destination."
                );
            }

            return new RoutingOverlay(
                    baseGraph,
                    SUPER_SOURCE_ID,
                    SUPER_DESTINATION_ID,
                    virtualPoints,
                    addedAdjacency,
                    destinationPoints
            );
        }
    }

    private record ProjectionNode(
            long nodeId,
            EdgeProjection projection
    ) {
    }

    private record EdgeNodePosition(
            long nodeId,
            EdgePosition position
    ) {
    }
}
