package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.path.AStarPathFinder;
import com.example.campus_navigation_backend.domain.path.BestRoute;
import com.example.campus_navigation_backend.domain.path.PathResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 시작 후보와 목적지 출입구 조합 중 최적 경로를 선택하고 RouteResponse로 조립한다.
 */
@Component
@RequiredArgsConstructor
public class RouteResponseAssembler {

    private static final double DUPLICATE_POINT_TOLERANCE = 1e-4;
    private final CampusGraphStore campusGraphStore;
    private final AStarPathFinder pathFinder;

    /**
     * 모든 시작 후보와 목적지 출입구 조합을 탐색하여 가장 짧은 경로 응답을 만든다.
     *
     * @param destinationBuildingName 목적지 건물명
     * @param currentPoint metric 좌표계의 현재 위치
     * @param startCandidates 현재 위치 주변의 시작 후보 목록
     * @param targetEntranceNodeIds 목적지 건물의 출입구 노드 ID 목록
     * @return 최적 경로 응답
     */
    public RouteResponse assembleBestRoute(String destinationBuildingName,
                                           Point3D currentPoint,
                                           List<StartCandidateFinder.StartCandidate> startCandidates,
                                           List<Long> targetEntranceNodeIds) {

        if (startCandidates == null || startCandidates.isEmpty()) {
            throw new IllegalStateException("No start candidate node found near current location.");
        }

        if (targetEntranceNodeIds == null || targetEntranceNodeIds.isEmpty()) {
            throw new IllegalArgumentException("No reachable entrance node found for destination.");
        }

        BestRoute bestRoute = null;

        for (StartCandidateFinder.StartCandidate candidate : startCandidates) {
            long startNodeId = candidate.nodeId();

            for (Long targetEntranceNodeId : targetEntranceNodeIds) {
                PathResult pathResult = pathFinder.findPath(startNodeId, targetEntranceNodeId);

                if (!pathResult.found()) {
                    continue;
                }

                double approachDistance = candidate.distanceFromCurrent();
                double graphDistance = pathResult.totalCost();
                double totalDistance = approachDistance + graphDistance;

                List<Point3D> mergedPath = buildMergedPath(
                        currentPoint,
                        campusGraphStore.getNode(startNodeId),
                        pathResult.edges()
                );

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
     * 현재 위치, 시작 노드, A* 결과 엣지 geometry를 하나의 경로 좌표 목록으로 병합한다.
     *
     * @param currentPoint metric 좌표계의 현재 위치
     * @param startNode 선택된 시작 노드
     * @param edges A* 탐색으로 얻은 엣지 목록
     * @return 병합된 경로 좌표
     */
    private List<Point3D> buildMergedPath(Point3D currentPoint,
                                          GraphNode startNode,
                                          List<GraphEdge> edges) {

        List<Point3D> merged = new ArrayList<>();

        appendIfNeeded(merged, currentPoint);
        appendIfNeeded(merged, startNode.point());

        for (GraphEdge edge : edges) {
            appendGeometry(merged, edge.geometry());
        }

        return merged;
    }

    /**
     * 엣지 geometry의 좌표를 중복을 제거하며 병합 경로에 추가한다.
     *
     * @param merged 병합 중인 경로 좌표
     * @param geometry 추가할 엣지 geometry
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
     * 직전 좌표와 충분히 다른 좌표만 경로에 추가한다.
     *
     * @param merged 병합 중인 경로 좌표
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
        if (distance3D(last, point) > DUPLICATE_POINT_TOLERANCE) {
            merged.add(point);
        }
    }

    /**
     * 두 좌표 사이의 3D 거리를 계산한다.
     *
     * @param a 첫 번째 좌표
     * @param b 두 번째 좌표
     * @return 3D 거리
     */
    private double distance3D(Point3D a, Point3D b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * 도메인 좌표를 API 응답용 RoutePoint로 변환한다.
     *
     * @param points 도메인 좌표 목록
     * @return 응답용 좌표 목록
     */
    private List<RoutePoint> toRoutePoints(List<Point3D> points) {
        return points.stream()
                .map(point -> new RoutePoint(point.x(), point.y(), point.z()))
                .toList();
    }
}
