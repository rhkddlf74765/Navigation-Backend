package com.example.campus_navigation_backend.test_package.graphmap.dto;

/**
 * 세계 측지 좌표계의 지리 좌표 응답이다.
 */
public record GraphMapGeoPointResponse(
        double lon,
        double lat,
        double ele
) {
}
