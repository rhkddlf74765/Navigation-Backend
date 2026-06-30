package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * 지도 렌더링을 위해 WGS84로 변환된 그래프 엣지이다.
 */
public record GraphMapGeoEdgeResponse(
        long fromNodeId,
        long toNodeId,
        String highway,
        double cost,
        List<GraphMapGeoPointResponse> geometry
) {
}
