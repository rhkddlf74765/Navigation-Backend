package com.example.campus_navigation_backend.domain.path;

import com.example.campus_navigation_backend.domain.graph.RoutingArc;
import com.example.campus_navigation_backend.domain.graph.RoutingGraph;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

@Component
public class AStarPathFinder
        implements PathFinder {

    private static final double EPS = 1e-9;

    @Override
    public PathResult findPath(
            RoutingGraph graph,
            long startNodeId,
            long goalNodeId
    ) {
        if (!graph.containsNode(
                startNodeId
        )) {
            throw new IllegalArgumentException(
                    "Start node not found."
            );
        }

        if (!graph.containsNode(
                goalNodeId
        )) {
            throw new IllegalArgumentException(
                    "Goal node not found."
            );
        }

        if (startNodeId == goalNodeId) {
            return PathResult.success(
                    List.of()
            );
        }

        PriorityQueue<SearchState> open =
                new PriorityQueue<>(
                        Comparator
                                .comparingDouble(
                                        SearchState::fScore
                                )
                                .thenComparingDouble(
                                        SearchState::gScore
                                )
                );

        Map<Long, Double> bestGScore =
                new HashMap<>();

        Map<Long, RoutingArc>
                cameFromArc =
                new HashMap<>();

        bestGScore.put(
                startNodeId,
                0.0
        );

        open.add(
                new SearchState(
                        startNodeId,
                        0.0,
                        graph
                                .estimateMinimumCost(
                                        startNodeId,
                                        goalNodeId
                                )
                )
        );

        while (!open.isEmpty()) {

            SearchState current =
                    open.poll();

            double bestKnown =
                    bestGScore
                            .getOrDefault(
                                    current.nodeId(),
                                    Double.POSITIVE_INFINITY
                            );

            if (current.gScore()
                    > bestKnown + EPS) {
                continue;
            }

            if (current.nodeId()
                    == goalNodeId) {
                return reconstructPath(
                        startNodeId,
                        goalNodeId,
                        cameFromArc
                );
            }

            for (RoutingArc arc
                    : graph
                    .getAdjacency(
                            current.nodeId()
                    )) {

                double tentativeG =
                        current.gScore()
                                + arc.cost();

                double knownG =
                        bestGScore
                                .getOrDefault(
                                        arc.toNodeId(),
                                        Double.POSITIVE_INFINITY
                                );

                if (tentativeG + EPS
                        >= knownG) {
                    continue;
                }

                bestGScore.put(
                        arc.toNodeId(),
                        tentativeG
                );

                cameFromArc.put(
                        arc.toNodeId(),
                        arc
                );

                double fScore =
                        tentativeG
                                + graph
                                .estimateMinimumCost(
                                        arc.toNodeId(),
                                        goalNodeId
                                );

                open.add(
                        new SearchState(
                                arc.toNodeId(),
                                tentativeG,
                                fScore
                        )
                );
            }
        }

        return PathResult.unreachable();
    }

    private PathResult reconstructPath(
            long startNodeId,
            long goalNodeId,
            Map<Long, RoutingArc>
                    cameFromArc
    ) {
        List<RoutingArc> arcs =
                new ArrayList<>();

        long current =
                goalNodeId;

        while (current
                != startNodeId) {

            RoutingArc arc =
                    cameFromArc.get(
                            current
                    );

            if (arc == null) {
                return PathResult
                        .unreachable();
            }

            arcs.add(arc);

            current =
                    arc.fromNodeId();
        }

        Collections.reverse(arcs);

        return PathResult.success(
                arcs
        );
    }

    private record SearchState(
            long nodeId,
            double gScore,
            double fScore
    ) {
    }
}