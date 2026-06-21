package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 임의의 현재 위치에서 경로 탐색을 시작할 수 있는 그래프 노드 후보를 찾는다.
 */
@Component
@RequiredArgsConstructor
public class StartCandidateFinder {

    private final NavigationProperties properties;
    private final CampusGraphStore campusGraphStore;

    /**
     * 현재 위치 반경 안에 있는 시작 후보 노드를 가까운 순서로 조회한다.
     *
     * @param currentPoint metric 좌표계의 현재 위치
     * @return 시작 후보 노드와 현재 위치 사이의 거리 목록
     */
    public List<StartCandidate> findCandidates(Point3D currentPoint) {
        return campusGraphStore.getStartCandidateNodeIds().stream()
                .map(nodeId -> {
                    GraphNode node = campusGraphStore.getNode(nodeId);
                    double distance = currentPoint.distance2D(node.point());
                    return new StartCandidate(nodeId, distance);
                })
                .filter(candidate -> candidate.distanceFromCurrent() <= properties.candidateRadiusMeters())
                .sorted(Comparator.comparingDouble(StartCandidate::distanceFromCurrent))
                .limit(properties.maxStartCandidates())
                .toList();
    }

    /**
     * 경로 탐색을 시작할 수 있는 후보 노드와 현재 위치로부터의 접근 거리를 나타낸다.
     *
     * @param nodeId 시작 후보 노드 ID
     * @param distanceFromCurrent 현재 위치에서 후보 노드까지의 거리
     */
    public record StartCandidate(long nodeId, double distanceFromCurrent) {}
}
