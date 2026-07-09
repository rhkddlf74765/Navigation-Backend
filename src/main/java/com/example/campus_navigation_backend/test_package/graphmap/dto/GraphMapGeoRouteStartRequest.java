package com.example.campus_navigation_backend.test_package.graphmap.dto;

/**
 * 그래프 지도 경로 시각화를 시작할 때 사용하는 WGS84 출발 및 도착 좌표를 담는다.
 *
 * @param startPoint 경로 출발지로 선택된 지리 좌표
 * @param destinationPoint 경로 도착지로 선택된 지리 좌표
 */
public record GraphMapGeoRouteStartRequest(
        GraphMapGeoPointRequest startPoint,
        GraphMapGeoPointRequest destinationPoint
) {
}
