package com.example.campus_navigation_backend.application.routing.helper;

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
 * 접근 경로에서 A* 탐색 밖에 있는 부분 경로 구간을 생성한다.
 * <p>
 * 좌표와 투영점을 잇는 구간은 직선 metric 구간이며, 투영점과 노드를 잇는 구간은 실제 그래프 엣지 geometry를 따른다.
 * 엣지 geometry를 따르는 구간은 {@link EdgeCostPolicy}를 적용해 계단, 경사, 고도차에 따른 비용을 계산한다.
 */
@Component
@RequiredArgsConstructor
public class RouteSegmentBuilder {

    private static final double EPS = 1e-9;

    private final EdgeCostPolicy edgeCostPolicy;

    /**
     * 임의 좌표와 투영점 사이의 구간은 그래프 엣지 geometry 밖에 있으므로 두 점을 직선으로 연결한 구간을 만든다.
     */
    public RouteSegment connector(Point3D from, Point3D to) {
        return new RouteSegment(from.distance2D(to), List.of(from, to));
    }

    /**
     * 출발 endpoint를 A* 시작 노드로 연결할 수 있도록 투영점에서 엣지의 from 노드까지의 구간을 만든다.
     */
    public RouteSegment projectionToFromNode(PointProjection projection) {
        GraphEdge edge = projection.sourceEdge();
        List<Point3D> path = sliceFromProjectionToStart(edge.geometry(), projection.projectedPoint());
        return edgeSegment(path, edge.edgeType());
    }

    /**
     * 출발 endpoint를 A* 시작 노드로 연결할 수 있도록 투영점에서 엣지의 to 노드까지의 구간을 만든다.
     */
    public RouteSegment projectionToToNode(PointProjection projection) {
        GraphEdge edge = projection.sourceEdge();
        List<Point3D> path = sliceFromProjectionToEnd(edge.geometry(), projection.projectedPoint());
        return edgeSegment(path, edge.edgeType());
    }

    /**
     * 최종 경로가 그래프에서 요청 도착점으로 접근해야 하므로 엣지의 from 노드에서 투영점까지의 도착 측 구간을 만든다.
     */
    public RouteSegment fromNodeToProjection(PointProjection projection) {
        GraphEdge edge = projection.sourceEdge();
        List<Point3D> path = reverse(sliceFromProjectionToStart(edge.geometry(), projection.projectedPoint()));
        return edgeSegment(path, edge.edgeType());
    }

    /**
     * 최종 경로가 그래프에서 요청 도착점으로 접근해야 하므로 엣지의 to 노드에서 투영점까지의 도착 측 구간을 만든다.
     */
    public RouteSegment toNodeToProjection(PointProjection projection) {
        GraphEdge edge = projection.sourceEdge();
        List<Point3D> path = reverse(sliceFromProjectionToEnd(edge.geometry(), projection.projectedPoint()));
        return edgeSegment(path, edge.edgeType());
    }

    /**
     * 엣지 유형과 고도차 가중치를 적용하기 전에 실제 이동 길이가 필요하므로 잘라낸 경로의 3D 거리를 합산한다.
     */
    private RouteSegment edgeSegment(List<Point3D> path, String edgeType) {
        double baseCost = pathCost(path);
        double elevationDelta = path.get(path.size() - 1).ele() - path.get(0).ele();
        double adjustedCost = edgeCostPolicy.calculate(edgeType, edgeType, baseCost, elevationDelta);
        return new RouteSegment(adjustedCost, path);
    }

    /**
     * 투영점에서 from 노드 방향으로 이동하는 경로를 만들기 위해 투영점을 포함하고 geometry 앞부분을 거꾸로 따라간다.
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
     * 투영점에서 to 노드 방향으로 이동하는 경로를 만들기 위해 투영점을 포함하고 geometry 뒷부분을 따라간다.
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
     * 올바른 polyline 위치에서 자르기 시작할 수 있도록 투영점과 가장 가까운 geometry segment를 찾는다.
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

    /**
     * 가장 가까운 엣지 geometry segment를 찾기 위해 점과 선분 사이의 2D 거리를 계산한다.
     */
    private double distanceToSegment(Point3D point, Point3D start, Point3D end) {
        double dx = end.lon() - start.lon();
        double dy = end.lat() - start.lat();
        double lengthSquared = dx * dx + dy * dy;
        double t = 0.0;
        if (lengthSquared > EPS) {
            t = ((point.lon() - start.lon()) * dx + (point.lat() - start.lat()) * dy) / lengthSquared;
            t = Math.max(0.0, Math.min(1.0, t));
        }
        Point3D closest = new Point3D(
                start.lon() + (end.lon() - start.lon()) * t,
                start.lat() + (end.lat() - start.lat()) * t,
                start.ele() + (end.ele() - start.ele()) * t
        );
        return point.distance2D(closest);
    }

    /**
     * 엣지 정체성과 경사 가중치를 적용하기 전에 실제 이동 거리를 구해야 하므로 잘라낸 경로의 3D 거리를 합산한다.
     */
    private double pathCost(List<Point3D> path) {
        double cost = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            cost += path.get(i).distance3D(path.get(i + 1));
        }
        return cost;
    }

    /**
     * 투영점에서 노드로 향하도록 자른 경로를 노드에서 투영점으로 향하는 방향에서도 사용할 수 있도록 뒤집는다.
     */
    private List<Point3D> reverse(List<Point3D> path) {
        List<Point3D> copied = new ArrayList<>(path);
        Collections.reverse(copied);
        return copied;
    }

    /**
     * 투영점과 엣지 vertex에서 중복 좌표가 생기지 않도록 이전 점과 다른 경우에만 geometry 점을 추가한다.
     */
    private void appendIfNeeded(List<Point3D> path, Point3D point) {
        if (path.isEmpty() || path.get(path.size() - 1).distance3D(point) > EPS) {
            path.add(point);
        }
    }

    /**
     * 투영점과 가장 가까운 엣지 geometry segment의 인덱스를 보관한다.
     */
    private record ProjectionLocation(int segmentStartIndex) {
    }
}
