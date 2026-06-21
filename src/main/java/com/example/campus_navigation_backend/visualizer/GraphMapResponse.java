package com.example.campus_navigation_backend.visualizer;

import java.util.List;

/**
 * 그래프 전체를 지도 위에 표시하기 위한 응답 DTO이다.
 *
 * @param nodes 점으로 표시할 그래프 노드 목록
 * @param edges 선으로 표시할 그래프 엣지 목록
 */
public record GraphMapResponse(
        List<GraphMapNode> nodes,
        List<GraphMapEdge> edges
) {
}
