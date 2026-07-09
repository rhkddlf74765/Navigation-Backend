package com.example.campus_navigation_backend.test_package.graphmap.dto;

import java.util.List;

/**
 * 그래프 지도 화면이 렌더링할 경로 형상과 전체 비용을 담는다.
 *
 * @param found 경로를 찾았는지 여부
 * @param totalCost application 라우팅 facade가 반환한 전체 라우팅 비용
 * @param path 지도에 렌더링할 WGS84 경로 polyline 좌표
 */
public record GraphMapRouteResultResponse(
        boolean found,
        double totalCost,
        List<GraphMapGeoPointResponse> path
) {
}
