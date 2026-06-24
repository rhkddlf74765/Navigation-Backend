package com.example.campus_navigation_backend.domain.projection;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 임의의 포인트를 그래프 엣지 geometry 위의 가장 가까운 지점으로 투영한다.
 */
@Component
public class PointProjector {

    private static final double EPS = 1e-9;

    /**
     * 포인트를 엣지의 polyline geometry 위에 투영한다.
     *
     * @param point 투영할 원본 포인트
     * @param edge 투영 대상 엣지
     * @return 투영 결과
     */
    public PointProjection project(Point3D point, GraphEdge edge) {
        List<Point3D> geometry = edge.geometry();
        if (geometry == null || geometry.size() < 2) {
            throw new IllegalArgumentException("Edge geometry must have at least two points.");
        }

        ProjectionCandidate best = null;
        double accumulatedCost = 0.0;

        for (int i = 0; i < geometry.size() - 1; i++) {
            Point3D segmentStart = geometry.get(i);
            Point3D segmentEnd = geometry.get(i + 1);
            double segmentCost = segmentStart.distance3D(segmentEnd);

            ProjectionCandidate candidate = projectToSegment(
                    point,
                    segmentStart,
                    segmentEnd,
                    accumulatedCost,
                    segmentCost
            );

            if (best == null || candidate.distanceFromSource() < best.distanceFromSource()) {
                best = candidate;
            }

            accumulatedCost += segmentCost;
        }

        double totalGeometryCost = accumulatedCost;
        return new PointProjection(
                point,
                best.projectedPoint(),
                edge,
                best.distanceFromSource(),
                best.costFromEdgeStart(),
                totalGeometryCost - best.costFromEdgeStart()
        );
    }

    /**
     * 포인트를 단일 segment 위에 투영한다.
     *
     * @param point 투영할 원본 포인트
     * @param segmentStart segment 시작점
     * @param segmentEnd segment 끝점
     * @param accumulatedCost segment 시작점까지 누적된 비용
     * @param segmentCost segment 비용
     * @return segment 투영 후보
     */
    private ProjectionCandidate projectToSegment(Point3D point,
                                                 Point3D segmentStart,
                                                 Point3D segmentEnd,
                                                 double accumulatedCost,
                                                 double segmentCost) {
        double dx = segmentEnd.x() - segmentStart.x();
        double dy = segmentEnd.y() - segmentStart.y();
        double lengthSquared = dx * dx + dy * dy;

        double fraction = 0.0;
        if (lengthSquared > EPS) {
            fraction = ((point.x() - segmentStart.x()) * dx + (point.y() - segmentStart.y()) * dy) / lengthSquared;
            fraction = Math.max(0.0, Math.min(1.0, fraction));
        }

        Point3D projectedPoint = interpolate(segmentStart, segmentEnd, fraction);
        double distanceFromSource = point.distance2D(projectedPoint);
        double costFromEdgeStart = accumulatedCost + segmentCost * fraction;

        return new ProjectionCandidate(projectedPoint, distanceFromSource, costFromEdgeStart);
    }

    /**
     * 두 좌표 사이의 보간 좌표를 계산한다.
     *
     * @param start 시작 좌표
     * @param end 끝 좌표
     * @param fraction 보간 비율
     * @return 보간된 좌표
     */
    private Point3D interpolate(Point3D start, Point3D end, double fraction) {
        return new Point3D(
                start.x() + (end.x() - start.x()) * fraction,
                start.y() + (end.y() - start.y()) * fraction,
                start.z() + (end.z() - start.z()) * fraction
        );
    }

    private record ProjectionCandidate(
            Point3D projectedPoint,
            double distanceFromSource,
            double costFromEdgeStart
    ) {
    }
}
