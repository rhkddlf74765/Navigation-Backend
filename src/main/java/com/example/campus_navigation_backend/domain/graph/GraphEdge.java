package com.example.campus_navigation_backend.domain.graph;

import java.util.List;

/**
 * 캠퍼스 그래프에서 두 노드를 연결하는 방향성 엣지이다.
 *
 * @param fromNodeId 시작 노드 ID
 * @param toNodeId 도착 노드 ID
 * @param cost 경로 탐색에 사용하는 비용
 * @param edgeType 엣지 유형 또는 highway 값
 * @param geometry 엣지의 실제 좌표 geometry
 */
public record GraphEdge (
        long fromNodeId,
        long toNodeId,
        double cost,
        String edgeType,
        List<Point3D> geometry
){
}
