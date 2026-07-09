package com.example.campus_navigation_backend.test_package.graphmap.dto;

/**
 * 출발 지점과 도착 지점을 포함하는 라우팅 디버그 요청이다.
 */
public record GraphMapRouteStartRequest(
        GraphMapPointRequest startPoint,
        GraphMapPointRequest destinationPoint
) {
}
