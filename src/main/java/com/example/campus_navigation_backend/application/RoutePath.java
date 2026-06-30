package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.path.PathResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 비용과 좌표열을 함께 가지는 라우팅 경로 조각이다.
 * <p>
 * 다른 RoutePath와 결합할 때 비용을 합산하고, 이어 붙는 좌표의 중복을 제거한다.
 *
 * @param cost 경로 비용
 * @param points 경로를 구성하는 metric 좌표 목록
 */
public record RoutePath(
        double cost,
        List<Point3D> points
) {
    private static final double DUPLICATE_POINT_TOLERANCE = 1e-4;

    /**
     * A* 탐색 결과의 엣지 geometry를 하나의 RoutePath로 변환한다.
     *
     * @param pathResult A* 탐색 결과
     * @return 그래프 내부 경로 조각
     */
    public static RoutePath from(PathResult pathResult) {
        List<Point3D> points = new ArrayList<>();
        for (GraphEdge edge : pathResult.edges()) {
            append(points, edge.geometry());
        }
        return new RoutePath(pathResult.totalCost(), points);
    }

    /**
     * 현재 경로 뒤에 다음 경로를 이어 붙인다.
     *
     * @param next 뒤에 붙일 경로
     * @return 비용과 좌표열이 병합된 새 RoutePath
     */
    public RoutePath append(RoutePath next) {
        List<Point3D> merged = new ArrayList<>();
        append(merged, points);
        append(merged, next.points());
        return new RoutePath(cost + next.cost(), merged);
    }

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
