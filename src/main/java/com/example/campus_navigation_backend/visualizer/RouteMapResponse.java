package com.example.campus_navigation_backend.visualizer;

import java.util.List;

/**
 * 지도 시각화에 필요한 WGS84 시작점과 경로 좌표를 담는 응답 DTO이다.
 *
 * @param destinationBuildingName 목적지 건물명
 * @param selectedEntranceId 선택된 목적지 출입구 ID
 * @param totalDistanceMeters 전체 거리
 * @param approachDistanceMeters 현재 위치에서 시작 후보 노드까지의 접근 거리
 * @param graphDistanceMeters 그래프 위에서 탐색된 거리
 * @param startPoint 지도에 표시할 시작점
 * @param path 지도에 표시할 WGS84 경로
 */
public record RouteMapResponse(
        String destinationBuildingName,
        long selectedEntranceId,
        double totalDistanceMeters,
        double approachDistanceMeters,
        double graphDistanceMeters,
        MapPoint startPoint,
        List<MapPoint> path
){
}
