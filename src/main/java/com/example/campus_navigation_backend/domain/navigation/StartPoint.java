package com.example.campus_navigation_backend.domain.navigation;

import com.example.campus_navigation_backend.domain.graph.Point3D;

/**
 * 네비게이션 출발점을 표현하는 값 객체이다.
 * 출발점은 현재 위치 좌표이거나 건물명일 수 있다.
 *
 * @param type 출발점 유형
 * @param currentLocation 현재 위치 좌표
 * @param buildingName 출발 건물명
 */
public record StartPoint(
        StartPointType type,
        Point3D currentLocation,
        String buildingName
) {

    /**
     * 현재 위치 좌표를 출발점으로 생성한다.
     *
     * @param currentLocation metric 좌표계의 현재 위치
     * @return 현재 위치 출발점
     */
    public static StartPoint currentLocation(Point3D currentLocation) {
        if (currentLocation == null) {
            throw new IllegalArgumentException("Current location must not be null.");
        }

        return new StartPoint(StartPointType.CURRENT_LOCATION, currentLocation, null);
    }

    /**
     * 건물명을 출발점으로 생성한다.
     *
     * @param buildingName 출발 건물명
     * @return 건물 출발점
     */
    public static StartPoint building(String buildingName) {
        if (buildingName == null || buildingName.isBlank()) {
            throw new IllegalArgumentException("Building name must not be blank.");
        }

        return new StartPoint(StartPointType.BUILDING, null, buildingName);
    }
}
