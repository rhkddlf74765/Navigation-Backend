package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.path.AStarPathFinder;
import com.example.campus_navigation_backend.domain.path.BestRoute;
import com.example.campus_navigation_backend.domain.path.PathResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 응답용 경로 조립 담당
 */
@Component
public class RouteResponseAssembler {
    /**
     * 현재 위치 + 그래프 경로를 하나의 polyline으로 병합한다.
     * RouteResponse를 생성한다.
     */
    private static final double DUPLICATE_POINT_TOLERANCE = 1e-4;

    public RouteResponse assembleBestRoute(String destinationBuildingName,
                                           Point3D currentPoint,
                                           CampusGraph graph,
                                           List<StartCandidateFinder.StartCandidate> startCandidates,
                                           List<Long> targetEntranceNodeIds,
                                           AStarPathFinder pathFinder) {

        if (startCandidates == null || startCandidates.isEmpty()) {
            throw new IllegalStateException("현재 위치 반경 내 시작 후보 노드를 찾지 못했습니다.");
        }

        if (targetEntranceNodeIds == null || targetEntranceNodeIds.isEmpty()) {
            throw new IllegalArgumentException("도착 가능한 건물 입구 노드가 없습니다.");
        }

        BestRoute bestRoute = null;

        for (StartCandidateFinder.StartCandidate candidate : startCandidates) {
            long startNodeId = candidate.nodeId();

            for (Long targetEntranceNodeId : targetEntranceNodeIds) {
                PathResult pathResult = pathFinder.findPath(graph, startNodeId, targetEntranceNodeId);

                if (!pathResult.found()) {
                    continue;
                }

                double approachDistance = candidate.distanceFromCurrent();
                double graphDistance = pathResult.totalCost();
                double totalDistance = approachDistance + graphDistance;

                List<Point3D> mergedPath = buildMergedPath(
                        currentPoint,
                        graph.getNode(startNodeId),
                        pathResult.edges()
                );

                long selectedEntranceId = graph.getNode(targetEntranceNodeId).sourceId();

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
            throw new IllegalStateException("도착 가능한 경로를 찾지 못했습니다.");
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

    private void appendGeometry(List<Point3D> merged, List<Point3D> geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        for (Point3D point : geometry) {
            appendIfNeeded(merged, point);
        }
    }

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

    private double distance3D(Point3D a, Point3D b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private List<RoutePoint> toRoutePoints(List<Point3D> points) {
        return points.stream()
                .map(point -> new RoutePoint(point.x(), point.y(), point.z()))
                .toList();
    }
}
