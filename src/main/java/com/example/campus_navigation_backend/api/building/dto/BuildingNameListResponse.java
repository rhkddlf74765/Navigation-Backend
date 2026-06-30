package com.example.campus_navigation_backend.api.building.dto;

import java.util.List;

/**
 * 클라이언트가 출발지 또는 목적지 선택에 사용할 수 있는 건물명 목록 응답이다.
 *
 * @param buildings 정렬된 표시용 건물명 목록
 */
public record BuildingNameListResponse(
        List<String> buildings
) {
}
