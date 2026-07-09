package com.example.campus_navigation_backend.log.domain;

/**
 * 라우팅 endpoint가 어떤 입력 방식으로 전달되었는지 나타낸다.
 */
public enum RouteEndpointLogType {
    /** 사용자가 직접 지정한 좌표 endpoint */
    COORDINATE,

    /** 건물명으로 지정한 endpoint */
    BUILDING
}
