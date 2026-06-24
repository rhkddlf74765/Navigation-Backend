package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

/**
 * 임의 출발점이 그래프 엣지에 투영된 뒤 실제 A* 시작 노드로 연결되는 후보이다.
 *
 * @param nodeId A*를 시작할 기존 그래프 노드 ID
 * @param accessCost 원본 출발점에서 투영점을 거쳐 nodeId까지 도달하는 비용
 * @param accessPath 원본 출발점, 투영점, 후보 노드를 잇는 응답용 경로 좌표
 */
public record RouteEndpointCandidate(
        long nodeId,
        double accessCost,
        List<Point3D> accessPath
) {
}
