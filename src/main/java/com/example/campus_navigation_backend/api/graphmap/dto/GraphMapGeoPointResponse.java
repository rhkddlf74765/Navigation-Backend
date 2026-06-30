package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * 세계 측지 좌표계의 지리 좌표 응답이다.
 */
public record GraphMapGeoPointResponse(
        double longitude,
        double latitude,
        double altitude
) {
}
