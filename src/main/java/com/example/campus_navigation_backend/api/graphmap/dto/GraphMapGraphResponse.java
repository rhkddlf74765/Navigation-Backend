package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * 웹 페이지에서 캠퍼스 그래프를 시각화하기 위한 전체 그래프 스냅샷이다.
 */
public record GraphMapGraphResponse(
        List<GraphMapNodeResponse> nodes,
        List<GraphMapEdgeResponse> edges
) {
}
