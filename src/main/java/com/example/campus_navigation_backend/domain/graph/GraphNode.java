package com.example.campus_navigation_backend.domain.graph;

/**
 * CampusGraph를 구성하는 노드이다.
 *
 * @param id 그래프 내부 노드 ID
 * @param type 노드 유형
 * @param sourceId 원본 데이터 ID 또는 그래프 내부 ID
 * @param point 노드 좌표
 */
public record GraphNode(
        long id,
        GraphNodeType type,
        long sourceId,
        Point3D point
) {
}
