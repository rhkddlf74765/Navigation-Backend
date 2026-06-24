package com.example.campus_navigation_backend.domain.path;

import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * CampusGraphStore를 통해 그래프를 읽고 A* 알고리즘으로 최단 경로를 탐색한다.
 */
@Component
@RequiredArgsConstructor
public class AStarPathFinder {

    private static final double EPS = 1e-6;
    private final CampusGraphStore campusGraphStore;

    /**
     * 출발 노드에서 도착 노드까지의 최단 경로를 탐색한다.
     *
     * @param startNodeId 출발 노드 ID
     * @param goalNodeId 도착 노드 ID
     * @return 경로 탐색 결과
     */
    public PathResult findPath(long startNodeId, long goalNodeId) {
        PriorityQueue<SearchState> open = new PriorityQueue<>(Comparator.comparingDouble(SearchState::fScore));
        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, Long> cameFromNode = new HashMap<>();
        Map<Long, GraphEdge> cameFromEdge = new HashMap<>();
        int visitedCount = 0;

        gScore.put(startNodeId, 0.0);
        open.add(new SearchState(startNodeId, heuristic(startNodeId, goalNodeId)));

        while (!open.isEmpty()) {
            SearchState current = open.poll();
            visitedCount++;

            if (current.nodeId() == goalNodeId) {
                System.out.println(
                        "A* reached goal. start=" + startNodeId
                                + ", goal=" + goalNodeId
                                + ", visited=" + visitedCount
                );
                return reconstructPath(startNodeId, goalNodeId, cameFromNode, cameFromEdge, gScore.get(goalNodeId));
            }

            for (GraphEdge edge : campusGraphStore.getAdjacency(current.nodeId())) {
                double tentative = gScore.get(current.nodeId()) + edge.cost();

                if (tentative + EPS < gScore.getOrDefault(edge.toNodeId(), Double.POSITIVE_INFINITY)) {
                    gScore.put(edge.toNodeId(), tentative);
                    cameFromNode.put(edge.toNodeId(), current.nodeId());
                    cameFromEdge.put(edge.toNodeId(), edge);

                    double fScore = tentative + heuristic(edge.toNodeId(), goalNodeId);
                    open.add(new SearchState(edge.toNodeId(), fScore));
                }
            }
        }

        System.out.println(
                "A* unreachable. start=" + startNodeId
                        + ", goal=" + goalNodeId
                        + ", visited=" + visitedCount
        );
        return PathResult.unreachable();
    }

    /**
     * A* 휴리스틱으로 사용할 현재 노드와 목표 노드 사이의 2D 직선 거리를 계산한다.
     *
     * @param nodeId 현재 노드 ID
     * @param goalNodeId 목표 노드 ID
     * @return 휴리스틱 거리
     */
    private double heuristic(long nodeId, long goalNodeId) {
        return campusGraphStore.distance2D(nodeId, goalNodeId);
    }

    /**
     * 탐색 중 기록한 이전 노드와 엣지를 따라 최종 경로를 복원한다.
     *
     * @param startNodeId 출발 노드 ID
     * @param goalNodeId 도착 노드 ID
     * @param cameFromNode 각 노드의 이전 노드 매핑
     * @param cameFromEdge 각 노드로 진입할 때 사용한 엣지 매핑
     * @param totalCost 최종 누적 비용
     * @return 복원된 경로 결과
     */
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
