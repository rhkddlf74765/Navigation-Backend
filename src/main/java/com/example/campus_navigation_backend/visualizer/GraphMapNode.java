package com.example.campus_navigation_backend.visualizer;

import com.example.campus_navigation_backend.domain.graph.GraphNodeType;

/**
 * 지도에 점으로 표시할 그래프 노드 정보이다.
 *
 * @param nodeId 그래프 노드 ID
 * @param type 노드 유형
 * @param point WGS84 좌표
 */
public record GraphMapNode(
        long nodeId,
        GraphNodeType type,
        MapPoint point
) {
}
