package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * 그래프 지도 뷰어에서 사용하는 그래프 엣지 스냅샷이다.
 */
public record GraphMapEdgeResponse(
        long fromNodeId,
        long toNodeId,
        String highway,
        double cost,
        List<GraphMapPointResponse> geometry
) {
}
