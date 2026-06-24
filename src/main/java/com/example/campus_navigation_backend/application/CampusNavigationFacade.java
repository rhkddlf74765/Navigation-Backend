package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.navigation.DestPoint;
import com.example.campus_navigation_backend.domain.navigation.StartPoint;
import com.example.campus_navigation_backend.domain.projection.PointProjection;
import com.example.campus_navigation_backend.domain.projection.PointProjector;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 경로 탐색 유스케이스를 조율하는 application facade이다.
 */
@Service
@RequiredArgsConstructor
public class CampusNavigationFacade {

    private final NavigationRepository navigationRepository;
    private final CampusGraphStore campusGraphStore;
    private final NavigationProperties properties;
    private final PointProjector pointProjector;
    private final RouteResponseAssembler routeResponseAssembler;

    /**
     * 요청 좌표를 metric 좌표로 변환하고 projection 기반 출발 후보와 목적지 후보를 조합해 경로를 찾는다.
     *
     * @param request 경로 탐색 요청
     * @return 최단 경로 응답
     */
    public RouteResponse findRoute(RouteRequest request) {
        StartPoint startPoint = StartPoint.currentLocation(transformCurrentPoint(request));
        DestPoint destPoint = new DestPoint(request.destinationBuildingName());

        List<Long> targetEntranceNodeIds =
                campusGraphStore.findEntranceNodeIdsByBuildingName(destPoint.buildingName());
        if (targetEntranceNodeIds.isEmpty()) {
            throw new IllegalArgumentException("No entrance found for destination building.");
        }

        List<RouteEndpointCandidate> candidates =
                findProjectedStartCandidates(startPoint.currentLocation());

        return routeResponseAssembler.assembleBestRoute(
                destPoint.buildingName(),
                startPoint.currentLocation(),
                candidates,
                targetEntranceNodeIds
        );
    }

    /**
     * 현재 위치를 주변 엣지에 투영하고, 각 투영점에서 엣지 양 끝 노드로 이동하는 출발 후보를 만든다.
     *
     * @param currentPoint metric 좌표계의 현재 위치
     * @return projection 기반 출발 후보 목록
     */
    private List<RouteEndpointCandidate> findProjectedStartCandidates(Point3D currentPoint) {
        Map<Long, RouteEndpointCandidate> bestCandidateByNodeId = new HashMap<>();

        for (GraphEdge edge : campusGraphStore.getEdges()) {
            PointProjection projection = pointProjector.project(currentPoint, edge);
            if (projection.distanceFromSource() > properties.candidateRadiusMeters()) {
                continue;
            }

            registerCandidate(
                    bestCandidateByNodeId,
                    edge.fromNodeId(),
                    projection.distanceFromSource() + projection.costFromEdgeStart(),
                    List.of(currentPoint, projection.projectedPoint(), campusGraphStore.getNode(edge.fromNodeId()).point())
            );
            registerCandidate(
                    bestCandidateByNodeId,
                    edge.toNodeId(),
                    projection.distanceFromSource() + projection.costToEdgeEnd(),
                    List.of(currentPoint, projection.projectedPoint(), campusGraphStore.getNode(edge.toNodeId()).point())
            );
        }

        return bestCandidateByNodeId.values().stream()
                .sorted(Comparator.comparingDouble(RouteEndpointCandidate::accessCost))
                .limit(properties.maxStartCandidates())
                .toList();
    }

    /**
     * 같은 그래프 노드로 이어지는 후보 중 접근 비용이 더 낮은 후보만 유지한다.
     *
     * @param bestCandidateByNodeId 노드별 최적 후보 맵
     * @param nodeId 후보 노드 ID
     * @param cost 접근 비용
     * @param accessPath 접근 경로
     */
    private void registerCandidate(Map<Long, RouteEndpointCandidate> bestCandidateByNodeId,
                                   long nodeId,
                                   double cost,
                                   List<Point3D> accessPath) {
        RouteEndpointCandidate current = bestCandidateByNodeId.get(nodeId);
        if (current == null || cost < current.accessCost()) {
            bestCandidateByNodeId.put(nodeId, new RouteEndpointCandidate(nodeId, cost, accessPath));
        }
    }

    /**
     * 요청의 WGS84 좌표를 경로 계산용 metric 좌표로 변환한다.
     *
     * @param request 경로 탐색 요청
     * @return metric 좌표계의 현재 위치
     */
    private Point3D transformCurrentPoint(RouteRequest request) {
        TransformedPointRow transformed = navigationRepository.transformToMetric(
                request.longitude(),
                request.latitude(),
                request.altitude() == null ? 0.0 : request.altitude()
        );

        return transformed.toPoint3D();
    }
}
