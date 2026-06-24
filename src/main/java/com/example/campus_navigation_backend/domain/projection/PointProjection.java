package com.example.campus_navigation_backend.domain.projection;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;

/**
 * 임의의 포인트를 그래프 엣지 위에 투영한 결과이다.
 *
 * @param sourcePoint 원본 포인트
 * @param projectedPoint 엣지 위 투영점
 * @param sourceEdge 투영 대상 엣지
 * @param distanceFromSource 원본 포인트에서 투영점까지의 2D 거리
 * @param costFromEdgeStart 엣지 시작점에서 투영점까지의 edge geometry 상 비용
 * @param costToEdgeEnd 투영점에서 엣지 끝점까지의 edge geometry 상 비용
 */
public record PointProjection(
        Point3D sourcePoint,
        Point3D projectedPoint,
        GraphEdge sourceEdge,
        double distanceFromSource,
        double costFromEdgeStart,
        double costToEdgeEnd
) {
}
