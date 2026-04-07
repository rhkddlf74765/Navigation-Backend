package com.example.campus_navigation_backend.domain.path;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * A* 알고리즘 수행
 */
@Component
public class AStarPathFinder {

    private static final double EPS = 1e-6;

    public PathResult findPath(CampusGraph graph, long startNodeId, long goalNodeId) {
        PriorityQueue<SearchState> open = new PriorityQueue<>(Comparator.comparingDouble(SearchState::fScore));
        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, Long> cameFromNode = new HashMap<>();
        Map<Long, GraphEdge> cameFromEdge = new HashMap<>();

        gScore.put(startNodeId, 0.0);
        open.add(new SearchState(startNodeId, heuristic(graph, startNodeId, goalNodeId)));

        while (!open.isEmpty()) {
            SearchState current = open.poll();

            if (current.nodeId() == goalNodeId) {
                return reconstructPath(startNodeId, goalNodeId, cameFromNode, cameFromEdge, gScore.get(goalNodeId));
            }

            for (GraphEdge edge : graph.getAdjacency(current.nodeId())) {
                double tentative = gScore.get(current.nodeId()) + edge.cost();

                if (tentative + EPS < gScore.getOrDefault(edge.toNodeId(), Double.POSITIVE_INFINITY)) {
                    gScore.put(edge.toNodeId(), tentative);
                    cameFromNode.put(edge.toNodeId(), current.nodeId());
                    cameFromEdge.put(edge.toNodeId(), edge);

                    double fScore = tentative + heuristic(graph, edge.toNodeId(), goalNodeId);
                    open.add(new SearchState(edge.toNodeId(), fScore));
                }
            }
        }

        return PathResult.unreachable();
    }

    private double heuristic(CampusGraph graph, long nodeId, long goalNodeId) {
        Point3D a = graph.getNode(nodeId).point();
        Point3D b = graph.getNode(goalNodeId).point();
        return a.distance2D(b);
    }

    private PathResult reconstructPath(long startNodeId,
                                       long goalNodeId,
                                       Map<Long, Long> cameFromNode,
                                       Map<Long, GraphEdge> cameFromEdge,
                                       double totalCost) {
        List<GraphEdge> edges = new ArrayList<>();
        long current = goalNodeId;

        while (current != startNodeId) {
            GraphEdge edge = cameFromEdge.get(current);
            if (edge == null) {
                return PathResult.unreachable();
            }
            edges.add(edge);
            current = cameFromNode.get(current);
        }

        Collections.reverse(edges);
        return PathResult.success(totalCost, edges);
    }
}
