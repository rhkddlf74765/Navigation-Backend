package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * 세계 측지 좌표계로 표현된 전체 그래프 스냅샷이다.
 */
public record GraphMapGeoGraphResponse(
        List<GraphMapGeoNodeResponse> nodes,
        List<GraphMapGeoEdgeResponse> edges
) {
}
