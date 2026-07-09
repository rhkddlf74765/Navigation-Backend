package com.example.campus_navigation_backend.test_package.graphmap.dto;

/**
 * 지도 렌더링을 위해 WGS84로 변환된 그래프 노드이다.
 */
public record GraphMapGeoNodeResponse(
        long id,
        String nodeType,
        String description,
        double lon,
        double lat,
        double ele
) {
}
