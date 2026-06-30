package com.example.campus_navigation_backend.application.routing.helper;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.path.PathResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 누적 비용과 metric 좌표열을 함께 가지는 라우팅 경로 조각을 표현한다.
 * <p>
 * 경계 지점의 중복 좌표를 제거하면서 다른 RoutePath를 이어 붙일 수 있으므로
 * 접근 경로와 A* 그래프 경로를 점진적으로 결합하는 데 사용한다.
 *
 * @param cost 이 경로의 누적 비용
 * @param points 이 경로를 구성하는 metric 좌표 목록
 */
public record RoutePath(
        double cost,
        List<Point3D> points
) {
    private static final double DUPLICATE_POINT_TOLERANCE = 1e-4;

    /**
     * 선택된 모든 그래프 엣지의 geometry를 하나로 이어 A* 결과를 RoutePath로 변환한다.
     */
    public static RoutePath from(PathResult pathResult) {
        List<Point3D> points = new ArrayList<>();
        for (GraphEdge edge : pathResult.edges()) {
            append(points, edge.geometry());
        }
        return new RoutePath(pathResult.totalCost(), points);
    }

    /**
     * 최종 경로가 여러 경로 조각을 점진적으로 결합해 만들어지므로 다음 경로를 이어 붙이고 비용을 합산한다.
     */
    public RoutePath append(RoutePath next) {
        List<Point3D> merged = new ArrayList<>();
        append(merged, points);
        append(merged, next.points());
        return new RoutePath(cost + next.cost(), merged);
    }

    /**
     * 반환 polyline이 연속적으로 유지되도록 null 값과 경계의 중복 좌표를 건너뛰며 좌표를 추가한다.
     */
    private static void append(List<Point3D> target, List<Point3D> source) {
        if (source == null) {
            return;
        }
        for (Point3D point : source) {
            if (point == null) {
                continue;
            }
            if (target.isEmpty() || target.get(target.size() - 1).distance3D(point) > DUPLICATE_POINT_TOLERANCE) {
                target.add(point);
            }
        }
    }
}
