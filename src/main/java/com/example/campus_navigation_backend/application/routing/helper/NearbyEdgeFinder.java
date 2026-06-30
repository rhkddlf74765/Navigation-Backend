package com.example.campus_navigation_backend.application.routing.helper;

import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.projection.PointProjector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 임의의 metric 좌표와 가까운 그래프 엣지 후보를 찾는다.
 * <p>
 * 현재 구현은 좌표를 모든 그래프 엣지에 투영한 뒤 원본 좌표와 투영점 사이의 거리가 가장 가까운 엣지를 반환한다.
 */
@Component
@RequiredArgsConstructor
public class NearbyEdgeFinder {

    private final CampusGraphStore campusGraphStore;
    private final PointProjector pointProjector;

    /**
     * 주어진 지점을 그래프 엣지에 투영하고 가장 가까운 후보를 반환한다.
     */
    public List<ProjectedEdgeCandidate> findNearbyEdges(Point3D point, int limit) {
        return campusGraphStore.getEdges().stream()
                .map(edge -> new ProjectedEdgeCandidate(edge, pointProjector.project(point, edge)))
                .sorted(Comparator.comparingDouble(candidate -> candidate.projection().distanceFromSource()))
                .limit(limit)
                .toList();
    }
}
