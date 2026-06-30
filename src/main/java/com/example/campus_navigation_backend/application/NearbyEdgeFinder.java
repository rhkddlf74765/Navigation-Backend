package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.projection.PointProjector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 임의의 좌표와 가까운 그래프 엣지 후보를 찾는다.
 * <p>
 * 각 엣지에 대해 projection을 수행한 뒤, 원본 좌표와 projection 점 사이의 거리가 짧은 순서로
 * 상위 후보만 반환한다.
 */
@Component
@RequiredArgsConstructor
public class NearbyEdgeFinder {

    private final CampusGraphStore campusGraphStore;
    private final PointProjector pointProjector;

    /**
     * 지정한 좌표와 가장 가까운 엣지 후보를 limit개 찾는다.
     *
     * @param point 탐색 기준 좌표
     * @param limit 반환할 엣지 후보 수
     * @return projection 거리 기준으로 정렬된 엣지 후보 목록
     */
    public List<ProjectedEdgeCandidate> findNearbyEdges(Point3D point, int limit) {
        return campusGraphStore.getEdges().stream()
                .map(edge -> new ProjectedEdgeCandidate(edge, pointProjector.project(point, edge)))
                .sorted(Comparator.comparingDouble(candidate -> candidate.projection().distanceFromSource()))
                .limit(limit)
                .toList();
    }
}
