package com.example.campus_navigation_backend.application.dto;

/**
 * 라우팅 endpoint가 어떤 입력 방식으로 전달되었는지 표현한다.
 */
public enum RouteEndpointType {
    /**
     * 경도, 위도, 선택 고도 값으로 endpoint를 해석한다.
     */
    COORDINATE,

    /**
     * 건물 지점 저장소에 등록된 건물명으로 endpoint를 해석한다.
     */
    BUILDING
}
