package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 현재 위치에서 시작 노드 후보를 찾는 역할을 담당한다.
 */
@Component
@RequiredArgsConstructor
public class StartCandidateFinder {
    /**
     * 그래프 노드 중 시작 후보 탐색
     * 특정 반경 이내 필터링 (candidate-radius-meters)
     * 거리순 정렬
     * 최대 개수 제한
     */
    private final NavigationProperties properties;

    public List<StartCandidate> findCandidates(CampusGraph graph, Point3D currentPoint) {
        return graph.getStartCandidateNodeIds().stream()
                .map(nodeId -> {
                    GraphNode node = graph.getNode(nodeId);
                    double distance = currentPoint.distance2D(node.point());
                    return new StartCandidate(nodeId, distance);
                })
                .filter(candidate -> candidate.distanceFromCurrent() <= properties.candidateRadiusMeters())
                .sorted(Comparator.comparingDouble(StartCandidate::distanceFromCurrent))
                .limit(properties.maxStartCandidates())
                .toList();
    }

    public record StartCandidate(long nodeId, double distanceFromCurrent) {}
}
