package com.example.campus_navigation_backend.visualizer;

import java.util.List;

/**
 * 지도 클릭 좌표를 가장 가까운 그래프 엣지에 투영한 결과이다.
 *
 * @param sourcePoint 사용자가 클릭한 WGS84 좌표
 * @param projectedPoint 엣지 위에 투영된 WGS84 좌표
 * @param fromNodeId 투영 대상 엣지의 시작 노드 ID
 * @param toNodeId 투영 대상 엣지의 끝 노드 ID
 * @param edgeType 투영 대상 엣지 타입
 * @param edgeCost 투영 대상 엣지 비용
 * @param distanceFromSource 클릭 좌표에서 투영점까지의 2D metric 거리
 * @param costFromEdgeStart 엣지 시작점에서 투영점까지의 비용
 * @param costToEdgeEnd 투영점에서 엣지 끝점까지의 비용
 * @param edgePath 투영 대상 엣지의 WGS84 좌표열
 */
public record ProjectionMapResponse(
        MapPoint sourcePoint,
        MapPoint projectedPoint,
        long fromNodeId,
        long toNodeId,
        String edgeType,
        double edgeCost,
        double distanceFromSource,
        double costFromEdgeStart,
        double costToEdgeEnd,
        List<MapPoint> edgePath
) {
}
