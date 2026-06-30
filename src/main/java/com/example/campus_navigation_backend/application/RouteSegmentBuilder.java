package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.path.EdgeCostPolicy;
import com.example.campus_navigation_backend.domain.projection.PointProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 좌표-projection 점, projection 점-엣지 endpoint 사이의 부분 경로를 생성한다.
 * <p>
 * projection 점과 엣지 endpoint 사이의 이동은 실제 그래프 엣지 위의 이동이므로
 * {@link EdgeCostPolicy}를 적용해 계단과 경사도 비용을 반영한다.
 */
@Component
@RequiredArgsConstructor
public class RouteSegmentBuilder {

    private static final double EPS = 1e-9;

    private final EdgeCostPolicy edgeCostPolicy;

    /**
     * 그래프 밖의 원본 좌표와 projection 점을 연결하는 진입/이탈 구간을 만든다.
     *
     * @param from 구간 시작점
     * @param to 구간 끝점
     * @return 두 점을 직선으로 연결한 부분 경로
     */
    public RouteSegment connector(Point3D from, Point3D to) {
        return new RouteSegment(from.distance2D(to), List.of(from, to));
    }

    /**
     * projection 점에서 엣지의 from 노드까지 이동하는 부분 경로를 만든다.
     *
     * @param projection 엣지 위 projection 결과
     * @return projection 점 -> from 노드 방향의 부분 경로
     */
    public RouteSegment projectionToFromNode(PointProjection projection) {
        GraphEdge edge = projection.sourceEdge();
        List<Point3D> path = sliceFromProjectionToStart(edge.geometry(), projection.projectedPoint());
        return edgeSegment(path, edge.edgeType());
    }

    /**
     * projection 점에서 엣지의 to 노드까지 이동하는 부분 경로를 만든다.
     *
     * @param projection 엣지 위 projection 결과
     * @return projection 점 -> to 노드 방향의 부분 경로
     */
    public RouteSegment projectionToToNode(PointProjection projection) {
        GraphEdge edge = projection.sourceEdge();
        List<Point3D> path = sliceFromProjectionToEnd(edge.geometry(), projection.projectedPoint());
        return edgeSegment(path, edge.edgeType());
    }

    /**
     * 엣지의 from 노드에서 projection 점까지 이동하는 도착 측 부분 경로를 만든다.
     *
     * @param projection 엣지 위 projection 결과
     * @return from 노드 -> projection 점 방향의 부분 경로
     */
    public RouteSegment fromNodeToProjection(PointProjection projection) {
        GraphEdge edge = projection.sourceEdge();
        List<Point3D> path = reverse(sliceFromProjectionToStart(edge.geometry(), projection.projectedPoint()));
        return edgeSegment(path, edge.edgeType());
    }

    /**
     * 엣지의 to 노드에서 projection 점까지 이동하는 도착 측 부분 경로를 만든다.
     *
     * @param projection 엣지 위 projection 결과
     * @return to 노드 -> projection 점 방향의 부분 경로
     */
    public RouteSegment toNodeToProjection(PointProjection projection) {
        GraphEdge edge = projection.sourceEdge();
        List<Point3D> path = reverse(sliceFromProjectionToEnd(edge.geometry(), projection.projectedPoint()));
        return edgeSegment(path, edge.edgeType());
    }

    /**
     * 엣지 geometry 일부 구간에 방향별 비용 정책을 적용해 부분 경로를 만든다.
     */
    private RouteSegment edgeSegment(List<Point3D> path, String edgeType) {
        double baseCost = pathCost(path);
        double elevationDelta = path.get(path.size() - 1).z() - path.get(0).z();
        double adjustedCost = edgeCostPolicy.calculate(edgeType, edgeType, baseCost, elevationDelta);
        return new RouteSegment(adjustedCost, path);
    }

    /**
     * projection 점에서 엣지 geometry의 시작점까지의 좌표열을 잘라낸다.
     */
    private List<Point3D> sliceFromProjectionToStart(List<Point3D> geometry, Point3D projectedPoint) {
        ProjectionLocation location = findProjectionLocation(geometry, projectedPoint);
        List<Point3D> path = new ArrayList<>();
        path.add(projectedPoint);
        for (int i = location.segmentStartIndex(); i >= 0; i--) {
            appendIfNeeded(path, geometry.get(i));
        }
        return path;
    }

    /**
     * projection 점에서 엣지 geometry의 끝점까지의 좌표열을 잘라낸다.
     */
    private List<Point3D> sliceFromProjectionToEnd(List<Point3D> geometry, Point3D projectedPoint) {
        ProjectionLocation location = findProjectionLocation(geometry, projectedPoint);
        List<Point3D> path = new ArrayList<>();
        path.add(projectedPoint);
        for (int i = location.segmentStartIndex() + 1; i < geometry.size(); i++) {
            appendIfNeeded(path, geometry.get(i));
        }
        return path;
    }

    /**
     * projection 점이 위치한 엣지 geometry segment를 찾는다.
     */
    private ProjectionLocation findProjectionLocation(List<Point3D> geometry, Point3D projectedPoint) {
        int bestSegmentStartIndex = 0;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < geometry.size() - 1; i++) {
            double distance = distanceToSegment(projectedPoint, geometry.get(i), geometry.get(i + 1));
            if (distance < bestDistance) {
                bestDistance = distance;
                bestSegmentStartIndex = i;
            }
        }

        return new ProjectionLocation(bestSegmentStartIndex);
    }

    private double distanceToSegment(Point3D point, Point3D start, Point3D end) {
        double dx = end.x() - start.x();
        double dy = end.y() - start.y();
        double lengthSquared = dx * dx + dy * dy;
        double t = 0.0;
        if (lengthSquared > EPS) {
            t = ((point.x() - start.x()) * dx + (point.y() - start.y()) * dy) / lengthSquared;
            t = Math.max(0.0, Math.min(1.0, t));
        }
        Point3D closest = new Point3D(
                start.x() + (end.x() - start.x()) * t,
                start.y() + (end.y() - start.y()) * t,
                start.z() + (end.z() - start.z()) * t
        );
        return point.distance2D(closest);
    }

    private double pathCost(List<Point3D> path) {
        double cost = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            cost += path.get(i).distance3D(path.get(i + 1));
        }
        return cost;
    }

    private List<Point3D> reverse(List<Point3D> path) {
        List<Point3D> copied = new ArrayList<>(path);
        Collections.reverse(copied);
        return copied;
    }

    private void appendIfNeeded(List<Point3D> path, Point3D point) {
        if (path.isEmpty() || path.get(path.size() - 1).distance3D(point) > EPS) {
            path.add(point);
        }
    }

    private record ProjectionLocation(int segmentStartIndex) {
    }
}
