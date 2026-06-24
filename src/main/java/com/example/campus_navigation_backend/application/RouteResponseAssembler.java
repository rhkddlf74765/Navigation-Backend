package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.path.AStarPathFinder;
import com.example.campus_navigation_backend.domain.path.BestRoute;
import com.example.campus_navigation_backend.domain.path.PathResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade가 준비한 출발 후보와 목적지 후보 조합을 비교하여 최단 RouteResponse를 조립한다.
 */
@Component
@RequiredArgsConstructor
public class RouteResponseAssembler {

    private static final double DUPLICATE_POINT_TOLERANCE = 1e-4;

    private final CampusGraphStore campusGraphStore;
    private final AStarPathFinder pathFinder;

    /**
     * 출발 후보와 목적지 노드 후보의 모든 조합에 대해 A*를 수행하고 가장 짧은 경로를 선택한다.
     *
     * @param destinationBuildingName 응답에 표시할 목적지 건물명
     * @param currentPoint 원본 출발점
     * @param startCandidates projection 기반 출발 후보 목록
     * @param targetEntranceNodeIds Facade가 조회한 목적지 노드 후보 목록
     * @return 최단 경로 응답
     */
    public RouteResponse assembleBestRoute(String destinationBuildingName,
                                           Point3D currentPoint,
                                           List<RouteEndpointCandidate> startCandidates,
                                           List<Long> targetEntranceNodeIds) {
        if (startCandidates == null || startCandidates.isEmpty()) {
            throw new IllegalStateException("No start candidate node found near current location.");
        }

        if (targetEntranceNodeIds == null || targetEntranceNodeIds.isEmpty()) {
            throw new IllegalArgumentException("No reachable entrance node found for destination.");
        }

        BestRoute bestRoute = null;

        System.out.println("startCandidates size=" + startCandidates.size());
        for (RouteEndpointCandidate startCandidate : startCandidates) {
            System.out.println(
                    "start node=" + startCandidate.nodeId()
                            + ", accessCost=" + startCandidate.accessCost()
                            + ", adj=" + campusGraphStore.getAdjacency(startCandidate.nodeId()).size()
                            + ", point=" + campusGraphStore.getNode(startCandidate.nodeId()).point()
            );
        }

        System.out.println("targetEntranceNodeIds size=" + targetEntranceNodeIds.size());
        for (Long targetEntranceNodeId : targetEntranceNodeIds) {
            System.out.println(
                    "target node=" + targetEntranceNodeId
                            + ", adj=" + campusGraphStore.getAdjacency(targetEntranceNodeId).size()
                            + ", point=" + campusGraphStore.getNode(targetEntranceNodeId).point()
            );
        }

        for (RouteEndpointCandidate startCandidate : startCandidates) {
            for (Long targetEntranceNodeId : targetEntranceNodeIds) {
                PathResult pathResult = pathFinder.findPath(startCandidate.nodeId(), targetEntranceNodeId);
                System.out.println(
                        "A* result start=" + startCandidate.nodeId()
                                + ", target=" + targetEntranceNodeId
                                + ", found=" + pathResult.found()
                                + ", cost=" + pathResult.totalCost()
                );
                if (!pathResult.found()) {
                    continue;
                }

                double approachDistance = startCandidate.accessCost();
                double graphDistance = pathResult.totalCost();
                double totalDistance = approachDistance + graphDistance;
                List<Point3D> mergedPath = buildMergedPath(startCandidate.accessPath(), pathResult.edges());

                long selectedEntranceId = campusGraphStore.getNode(targetEntranceNodeId).sourceId();
                BestRoute candidateRoute = new BestRoute(
                        selectedEntranceId,
                        totalDistance,
                        approachDistance,
                        graphDistance,
                        mergedPath
                );

                if (bestRoute == null || candidateRoute.totalDistanceMeters() < bestRoute.totalDistanceMeters()) {
                    bestRoute = candidateRoute;
                }
            }
        }

        if (bestRoute == null) {
            throw new IllegalStateException("No reachable route found.");
        }

        return new RouteResponse(
                destinationBuildingName,
                bestRoute.selectedEntranceId(),
                bestRoute.totalDistanceMeters(),
                bestRoute.approachDistanceMeters(),
                bestRoute.graphDistanceMeters(),
                toRoutePoints(bestRoute.path())
        );
    }

    /**
     * 출발점 접근 경로와 A* 결과 엣지 geometry를 하나의 path로 병합한다.
     *
     * @param accessPath 원본 출발점에서 A* 시작 노드까지의 접근 경로
     * @param edges A* 결과 엣지 목록
     * @return 응답에 사용할 병합 경로
     */
    private List<Point3D> buildMergedPath(List<Point3D> accessPath, List<GraphEdge> edges) {
        List<Point3D> merged = new ArrayList<>();

        appendGeometry(merged, accessPath);
        for (GraphEdge edge : edges) {
            appendGeometry(merged, edge.geometry());
        }

        return merged;
    }

    /**
     * geometry 좌표를 중복을 제거하며 병합 경로에 추가한다.
     *
     * @param merged 병합 중인 경로
     * @param geometry 추가할 geometry
     */
    private void appendGeometry(List<Point3D> merged, List<Point3D> geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        for (Point3D point : geometry) {
            appendIfNeeded(merged, point);
        }
    }

    /**
     * 직전 좌표와 충분히 다른 좌표만 추가한다.
     *
     * @param merged 병합 중인 경로
     * @param point 추가 후보 좌표
     */
    private void appendIfNeeded(List<Point3D> merged, Point3D point) {
        if (point == null) {
            return;
        }

        if (merged.isEmpty()) {
            merged.add(point);
            return;
        }

        Point3D last = merged.get(merged.size() - 1);
        if (last.distance3D(point) > DUPLICATE_POINT_TOLERANCE) {
            merged.add(point);
        }
    }

    /**
     * 도메인 좌표를 응답 DTO 좌표로 변환한다.
     *
     * @param points 도메인 좌표 목록
     * @return 응답 좌표 목록
     */
    private List<RoutePoint> toRoutePoints(List<Point3D> points) {
        return points.stream()
                .map(point -> new RoutePoint(point.x(), point.y(), point.z()))
                .toList();
    }
}
