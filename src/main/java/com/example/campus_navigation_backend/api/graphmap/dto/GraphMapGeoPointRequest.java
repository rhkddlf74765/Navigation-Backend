package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * 그래프 지도에서 사용하는 지리 좌표이다.
 */
public record GraphMapGeoPointRequest(
        double longitude,
        double latitude,
        Double altitude
) {
}
