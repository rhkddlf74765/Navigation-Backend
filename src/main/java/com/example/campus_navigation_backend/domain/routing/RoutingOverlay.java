package com.example.campus_navigation_backend.domain.routing;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.graph.RoutingArc;
import com.example.campus_navigation_backend.domain.graph.RoutingGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RoutingOverlay
        implements RoutingGraph {

    private final CampusGraph baseGraph;

    private final long startNodeId;

    private final long goalNodeId;

    private final Map<Long, MetricPoint>
            virtualPoints;

    private final Map<Long, List<RoutingArc>>
            addedAdjacency;

    private final List<MetricPoint>
            destinationPoints;

    public RoutingOverlay(
            CampusGraph baseGraph,
            long startNodeId,
            long goalNodeId,
            Map<Long, MetricPoint>
                    virtualPoints,
            Map<Long, List<RoutingArc>>
                    addedAdjacency,
            List<MetricPoint>
                    destinationPoints
    ) {
        this.baseGraph =
                baseGraph;

        this.startNodeId =
                startNodeId;

        this.goalNodeId =
                goalNodeId;

        this.virtualPoints =
                Map.copyOf(
                        virtualPoints
                );

        Map<Long, List<RoutingArc>>
                copied =
                new HashMap<>();

        addedAdjacency.forEach(
                (nodeId, arcs) ->
                        copied.put(
                                nodeId,
                                List.copyOf(arcs)
                        )
        );

        this.addedAdjacency =
                Map.copyOf(copied);

        this.destinationPoints =
                List.copyOf(
                        destinationPoints
                );
    }

    public long startNodeId() {
        return startNodeId;
    }

    public long goalNodeId() {
        return goalNodeId;
    }

    @Override
    public boolean containsNode(
            long nodeId
    ) {
        return baseGraph
                .containsNode(nodeId)

                || virtualPoints
                .containsKey(nodeId)

                || nodeId
                == startNodeId

                || nodeId
                == goalNodeId;
    }

    @Override
    public List<? extends RoutingArc>
    getAdjacency(
            long nodeId
    ) {
        List<RoutingArc> result =
                new ArrayList<>();

        if (baseGraph
                .containsNode(
                        nodeId
                )) {
            result.addAll(
                    baseGraph
                            .getAdjacency(
                                    nodeId
                            )
            );
        }

        result.addAll(
                addedAdjacency
                        .getOrDefault(
                                nodeId,
                                List.of()
                        )
        );

        return result;
    }

    @Override
    public double estimateMinimumCost(
            long fromNodeId,
            long ignoredGoalNodeId
    ) {
        if (fromNodeId == goalNodeId
                || fromNodeId
                == startNodeId) {
            return 0.0;
        }

        MetricPoint from =
                pointOf(
                        fromNodeId
                );

        if (from == null
                || destinationPoints
                .isEmpty()) {
            return 0.0;
        }

        double straightDistance =
                destinationPoints
                        .stream()
                        .mapToDouble(
                                from::distance2D
                        )
                        .min()
                        .orElse(0.0);

        /*
         * connector는 cost == distance이므로
         * base graph의 coefficient가 1보다 크더라도
         * overlay에서는 최대 1을 사용해야
         * admissibility가 유지된다.
         */
        double costPerMeter =
                Math.min(
                        1.0,
                        baseGraph
                                .heuristicCostPerMeter()
                );

        return straightDistance
                * costPerMeter;
    }

    private MetricPoint pointOf(
            long nodeId
    ) {
        MetricPoint virtual =
                virtualPoints.get(
                        nodeId
                );

        if (virtual != null) {
            return virtual;
        }

        var node =
                baseGraph.getNode(
                        nodeId
                );

        return node == null
                ? null
                : node.point();
    }
}
